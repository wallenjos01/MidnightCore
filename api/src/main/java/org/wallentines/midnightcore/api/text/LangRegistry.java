package org.wallentines.midnightcore.api.text;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.function.Supplier;

public class LangRegistry {

    protected final StringRegistry<String> entries = new StringRegistry<>();

    public void register(String key, String value) {

        entries.register(key, value);
    }

    public boolean hasKey(String key) {

        return entries.hasKey(key);
    }

    @Nullable
    public String getMessage(String key) {

        return getMessage(key, () -> null);
    }

    public String getMessage(String key, Supplier<String> def) {

        if(!entries.hasKey(key)) return def.get();

        return entries.get(key);
    }

    private void registerAll(ConfigSection sec, String prefix) {

        for(String key : sec.getKeys()) {
            ConfigObject obj = sec.get(key);
            if(obj.isString()) {

                register(prefix + key, obj.asString());

            } else if(obj.isSection()) {

                registerAll(obj.asSection(), prefix + key + ".");
            }
        }
    }

    public ConfigSection save() {

        ConfigSection out = new ConfigSection();
        for(String key : entries.getIds()) {
            out.set(key, entries.get(key));
        }

        return out;
    }

    public void fill(LangRegistry other) {

        for(String key : other.entries.getIds()) {
            if(!hasKey(key)) {
                register(key, other.getMessage(key));
            }
        }
    }

    public static LangRegistry fromConfigSection(ConfigSection sec) {

        LangRegistry out = new LangRegistry();
        out.registerAll(sec, "");

        return out;
    }
}
