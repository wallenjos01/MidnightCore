package org.wallentines.midnightcore.api.text;

import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

public class LangRegistry {

    protected final HashMap<String, Integer> indicesByKey = new HashMap<>();
    protected final List<LangEntry> entries = new ArrayList<>();

    public void register(String key, String value) {

        if(indicesByKey.containsKey(key)) throw new IllegalArgumentException("Attempt to overwrite lang entry with key " + key + "!");

        int index = entries.size();

        LangEntry ent = new LangEntry();
        ent.key = key;
        ent.value = value;

        entries.add(ent);
        indicesByKey.put(key, index);
    }

    public boolean hasKey(String key) {

        return indicesByKey.containsKey(key);
    }

    @Nullable
    public String getMessage(String key) {

        return getMessage(key, () -> null);
    }

    public String getMessage(String key, Supplier<String> def) {

        Integer index = indicesByKey.get(key);
        if(index == null) return def.get();

        return entries.get(index).value;
    }

    private void registerAll(ConfigSection sec, String prefix) {

        for(String key : sec.getKeys()) {
            Object obj = sec.get(key);
            if(obj instanceof String) {

                register(prefix + key, (String) obj);

            } else if(obj instanceof ConfigSection) {

                registerAll((ConfigSection) obj, prefix + key + ".");
            }
        }
    }

    public ConfigSection save() {

        ConfigSection out = new ConfigSection();
        for(LangEntry ent : entries) {
            out.set(ent.key, ent.value);
        }

        return out;
    }

    public static LangRegistry fromConfigSection(ConfigSection sec) {

        LangRegistry out = new LangRegistry();
        out.registerAll(sec, "");

        return out;
    }

    private static class LangEntry {

        String key;
        String value;
    }

}
