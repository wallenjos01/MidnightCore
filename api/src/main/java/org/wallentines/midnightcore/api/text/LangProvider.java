package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public class LangProvider {

    protected final File folder;
    protected final LangRegistry defaults;
    protected final String serverLocale;
    protected final HashMap<String, LangRegistry> registries = new HashMap<>();

    public LangProvider(Path folder, ConfigSection defaults) {
        this(folder.toFile(), LangRegistry.fromConfigSection(defaults), getServerLocale());
    }
    public LangProvider(Path folder, ConfigSection defaults, String serverLocale) {
        this(folder.toFile(), LangRegistry.fromConfigSection(defaults), serverLocale);
    }
    public LangProvider(File folder, LangRegistry defaults) {
        this(folder, defaults, getServerLocale());
    }
    public LangProvider(File folder, LangRegistry defaults, String serverLocale) {

        if(!folder.isDirectory()) throw new IllegalArgumentException("Attempt to create a provider with non-folder " + folder.getPath() + "!");

        this.folder = folder;
        this.defaults = defaults;
        this.serverLocale = serverLocale;

        FileConfig def = FileConfig.findOrCreate(serverLocale, folder);
        def.getRoot().fill(defaults.save());
        def.save();

        LangRegistry reg = LangRegistry.fromConfigSection(def.getRoot());
        registries.put(serverLocale, reg);
    }

    private LangRegistry getEntries(String key) {
        return registries.computeIfAbsent(key, lang -> {
            FileConfig conf = FileConfig.findFile(folder.listFiles(), lang);
            if(conf != null) {
                LangRegistry reg = LangRegistry.fromConfigSection(conf.getRoot());
                return registries.put(lang, reg);
            }
            return registries.get(serverLocale);
        });
    }

    public MComponent getMessage(String key, String locale, Object... args) {

        String message = getRawMessage(key, locale);
        return PlaceholderManager.INSTANCE.parseText(message, args);
    }

    public String getRawMessage(String key, String locale) {

        if(locale == null) locale = serverLocale;

        LangRegistry reg = getEntries(locale);
        return reg.getMessage(key, () -> {
            if(reg == registries.get(serverLocale)) {
                return key;
            }
            return getRawMessage(key, serverLocale);
        });
    }

    public boolean hasKey(String key) {
        return hasKey(key, serverLocale);
    }

    public boolean hasKey(String key, String locale) {
        return getEntries(locale).hasKey(key);
    }

    public MComponent getMessage(String key, MPlayer player, Object... args) {

        String locale = player == null ? serverLocale : player.getLocale();
        return getMessage(key, locale, args);
    }

    public String getRawMessage(String key, MPlayer player) {

        String locale = player == null ? serverLocale : player.getLocale();
        return getRawMessage(key, locale);
    }

    void reload() {

        registries.clear();

        FileConfig def = FileConfig.findOrCreate(serverLocale, folder);
        def.getRoot().fill(defaults.save());
        def.save();

        LangRegistry reg = LangRegistry.fromConfigSection(def.getRoot());
        registries.put(serverLocale, reg);
    }

    public static String getServerLocale() {
        return MidnightCoreAPI.getInstance() != null ? MidnightCoreAPI.getInstance().getServerLocale() : "en_us";
    }

}
