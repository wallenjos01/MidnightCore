package org.wallentines.midnightcore.common.module.lang;

import org.wallentines.midnightcore.api.module.lang.LangModule;
import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.util.FileUtil;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class LangProviderImpl implements LangProvider {

    private final HashMap<String, HashMap<String, String>> entries = new HashMap<>();
    private final HashMap<String, String> defaults;
    private final File dataFolder;
    private final LangModule module;

    public LangProviderImpl(Path folderPath, LangModule module, ConfigSection defaults) {

        this.dataFolder = FileUtil.tryCreateDirectory(folderPath);
        if(dataFolder == null) throw new IllegalStateException("Unable to create lang folder " + folderPath);

        this.module = module;
        this.defaults = fromConfigSection(defaults, "");

        saveDefaults(module.getServerLanguage());

    }

    private HashMap<String, String> getEntries(String language) {

        return entries.computeIfAbsent(language, k -> {

            FileConfig conf = FileConfig.findFile(dataFolder.listFiles(), language);
            if(conf == null) return new HashMap<>();

            return fromConfigSection(conf.getRoot(), "");
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

        HashMap<String, String> entries = getEntries(language);
        if(entries.containsKey(key)) return entries.get(key);

        return defaults.get(key);
    }

    @Override
    public boolean hasKey(String key) {

        return defaults.containsKey(key);
    }

    @Override
    public boolean hasKey(String key, String language) {

        return getEntries(language).containsKey(key);
    }

    @Override
    public void saveDefaults(String language) {

        FileConfig conf = FileConfig.findOrCreate(language, dataFolder);
        for(Map.Entry<String, String> ent : defaults.entrySet()) {
            conf.getRoot().set(ent.getKey(), ent.getValue());
        }
        conf.save();
    }

    @Override
    public void loadEntries(ConfigSection section, String language) {

        entries.put(language, fromConfigSection(section, ""));

        ConfigProvider prov = ConfigRegistry.INSTANCE.getDefaultProvider();
        FileConfig conf = new FileConfig(new File(dataFolder,language + prov.getFileExtension()), prov);
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

    private HashMap<String, String> fromConfigSection(ConfigSection section, String prefix) {

        HashMap<String, String> out = new HashMap<>();

        for(String key : section.getKeys()) {
            Object obj = section.get(key);
            if(obj instanceof String) {

                out.put(prefix + key, (String) obj);

            } else if(obj instanceof ConfigSection) {

                out.putAll(fromConfigSection((ConfigSection) obj, prefix + key + "."));
            }
        }

        return out;
    }

}
