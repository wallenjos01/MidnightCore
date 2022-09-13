package org.wallentines.midnightcore.common.module.lang;

import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class LangProviderImpl implements LangProvider {

    private final HashMap<String, LangRegistry> entries = new HashMap<>();
    private final LangRegistry defaults;

    private final File dataFolder;
    private final LangModule module;

    public LangProviderImpl(Path folderPath, LangModule module, ConfigSection defaults) {

        this.dataFolder = FileUtil.tryCreateDirectory(folderPath);
        if(dataFolder == null) throw new IllegalStateException("Unable to create lang folder " + folderPath);

        this.module = module;
        this.defaults = LangRegistry.fromConfigSection(defaults);

        fillDefaults(module.getServerLanguage());

    }

    private LangRegistry getEntries(String language) {

        return entries.computeIfAbsent(language, k -> {

            FileConfig conf = FileConfig.findFile(dataFolder.listFiles(), language);
            if(conf == null) return new LangRegistry();

            return LangRegistry.fromConfigSection(conf.getRoot());
        });

    }

    @Override
    public MComponent getMessage(String key, String language, Object... args) {

        String raw = getRawMessage(key, language);
        return module.parseText(raw, args);
    }

    @Override
    public String getRawMessage(String key, String language) {

        if(language == null) language = module.getServerLanguage();

        LangRegistry entries = getEntries(language);
        return entries.getMessage(key, () -> defaults.getMessage(key));
    }

    @Override
    public boolean hasKey(String key) {

        return defaults.hasKey(key);
    }

    @Override
    public boolean hasKey(String key, String language) {

        return getEntries(language).hasKey(key);
    }

    @Override
    public void fillDefaults(String language) {

        FileConfig conf = FileConfig.findOrCreate(language, dataFolder);
        conf.getRoot().fill(defaults.save());
        conf.save();
    }

    @Override
    public void saveDefaults(String language) {

        FileConfig conf = FileConfig.findOrCreate(language, dataFolder);
        conf.setRoot(defaults.save());
        conf.save();
    }

    @Override
    public void loadEntries(ConfigSection section, String language) {

        entries.put(language, LangRegistry.fromConfigSection(section));

        FileConfig conf = FileConfig.findOrCreate(language, dataFolder);

        conf.setRoot(section);
        conf.save();
    }

    @Override
    public LangModule getModule() {
        return module;
    }

    @Override
    public MComponent getMessage(String key, MPlayer player, Object... args) {
        return getMessage(key, player == null ? module.getServerLanguage() : player.getLocale(), args);
    }

    @Override
    public String getRawMessage(String key, MPlayer player) {
        return getRawMessage(key, player == null ? module.getServerLanguage() : player.getLocale());
    }

    @Override
    public void reload() {

        entries.clear();
    }

    private static class LangEntry {

        String key;
        String value;

    }

    private static class LangRegistry {

        HashMap<String, Integer> indicesByKey = new HashMap<>();
        List<LangEntry> entries = new ArrayList<>();

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


    }

}
