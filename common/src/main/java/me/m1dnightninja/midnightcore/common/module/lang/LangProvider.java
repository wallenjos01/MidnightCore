package me.m1dnightninja.midnightcore.common.module.lang;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.io.File;
import java.util.*;

public class LangProvider implements ILangProvider {

    protected final HashMap<String, HashMap<String, String>> entries = new HashMap<>();

    protected final File folder;
    protected final ILangModule module;

    protected final HashMap<String, String> defaults;

    public LangProvider(File folder, ILangModule mod, ConfigSection defaultEntries) {

        this.folder = folder;
        this.module = mod;

        defaults = generateDeepMap(defaultEntries, "");

        if(folder.exists() && !folder.isDirectory()) {
            MidnightCoreAPI.getLogger().warn("Unable to create lang folder at " + folder.getAbsolutePath() + "! Conflicting file exists!");
            return;
        }

        if(!folder.exists() && !folder.mkdirs()) {
            MidnightCoreAPI.getLogger().warn("Unable to create lang folder at " + folder.getAbsolutePath() + "!");
            return;
        }

        saveDefaults(mod.getServerLanguage());
        reloadAllEntries();

    }


    @Override
    public MComponent getMessage(String key, String language, Object... args) {

        String message = getRawMessage(key, language);
        message = module.applyInlinePlaceholders(message, args);

        MComponent comp = MComponent.Serializer.parse(message);
        comp = module.applyPlaceholders(comp, args);

        return comp;
    }

    @Override
    public MComponent getUnformattedMessage(String key, String language) {

        return MComponent.Serializer.parse(getRawMessage(key, language));
    }

    @Override
    public String getRawMessage(String key, String language) {

        String msg = getEntries(language).get(key);
        return msg == null ? defaults.get(key) : msg;

    }

    @Override
    public MComponent getMessage(String key, MPlayer player, Object... args) {

        return getMessage(key, module.getPlayerLocale(player), args);
    }

    @Override
    public MComponent getMessage(String key) {

        return getMessage(key, module.getServerLanguage());
    }

    @Override
    public void sendMessage(String key, MPlayer player, Object... args) {

        player.sendMessage(getMessage(key, player, args));
    }

    @Override
    public void sendMessage(String key, Iterable<MPlayer> players, Object... args) {

        for (MPlayer u : players) {

            sendMessage(key, u, args);
        }
    }

    @Override
    public void sendTitle(String key, MPlayer player, MTitle.TitleOptions opts, Object... args) {

        player.sendTitle(new MTitle(getMessage(key, player, args), opts));

    }

    @Override
    public void sendTitle(String key, Iterable<MPlayer> players, MTitle.TitleOptions opts, Object... args) {

        for(MPlayer u : players) {

            sendTitle(key, u, opts, args);
        }
    }

    @Override
    public void sendActionBar(String key, MPlayer player, MActionBar.ActionBarOptions opts, Object... args) {

        player.sendActionBar(new MActionBar(getMessage(key, player, args), opts));
    }

    @Override
    public void sendActionBar(String key, Iterable<MPlayer> players, MActionBar.ActionBarOptions opts, Object... args) {

        for(MPlayer u : players) {

            sendActionBar(key, u, opts, args);
        }
    }

    @Override
    public void reloadAllEntries() {

        entries.clear();
        File[] files = folder.listFiles();

        if(files != null) for(File f : files) {

            String lang = f.getName().substring(0, f.getName().lastIndexOf(".") - 1);
            if(entries.containsKey(lang)) continue;

            FileConfig conf = FileConfig.fromFile(f);
            if(conf != null) {

                entries.put(lang, generateDeepMap(conf.getRoot(), ""));
            }
        }
    }

    @Override
    public void saveDefaults(String file) {

        ConfigSection out = new ConfigSection();
        for(Map.Entry<String, String> ent : defaults.entrySet()) {
            out.set(ent.getKey(), ent.getValue());
        }

        FileConfig conf = FileConfig.findOrCreate(module.getServerLanguage(), folder);
        if(conf == null) return;

        conf.setRoot(out);
        conf.save();
    }

    @Override
    public boolean hasKey(String key) {
        return defaults.containsKey(key) || hasKey(key, module.getServerLanguage());
    }

    @Override
    public boolean hasKey(String key, String language) {
        return entries.containsKey(language) && entries.get(language).containsKey(key);
    }

    @Override
    public boolean hasKey(String key, MPlayer player) {
        return defaults.containsKey(key) || hasKey(key, module.getPlayerLocale(player));
    }

    @Override
    public ILangModule getModule() {
        return module;
    }

    private HashMap<String, String> getEntries(String locale) {

        return entries.computeIfAbsent(locale, k -> {

            String lang = locale.contains("_") ? locale.split("_")[0] : locale;
            for(String loc : entries.keySet()) {

                String lang2 = loc.contains("_") ? loc.split("_")[0] : loc;

                if(lang.equals(lang2)) {
                    return entries.get(loc);
                }
            }

            return defaults;
        });
    }

    private HashMap<String, String> generateDeepMap(ConfigSection section, String prefix) {

        HashMap<String, String> out = new HashMap<>();
        for(String s : section.getKeys()) {
            Object o = section.get(s);

            if(o instanceof String) {
                out.put(prefix + s, (String) o);

            } else if(o instanceof ConfigSection) {

                out.putAll(generateDeepMap((ConfigSection) o, prefix + s + "."));
            }
        }

        return out;
    }

}
