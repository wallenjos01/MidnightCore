package me.m1dnightninja.midnightcore.common.module.lang;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LangProvider implements ILangProvider {

    protected final HashMap<String, HashMap<String, String>> entries = new HashMap<>();

    protected final File folder;
    protected final ConfigProvider provider;
    protected final ILangModule module;

    protected final HashMap<String, String> defaults;

    public LangProvider(File folder, ILangModule mod, ConfigProvider provider, ConfigSection defaultEntries) {

        this.folder = folder;
        this.provider = provider;
        this.module = mod;

        defaults = loadEntries(defaultEntries);

        if(!folder.exists()) {
            if(!folder.mkdirs()) {
                MidnightCoreAPI.getLogger().warn("Unable to create Language folder!");
                return;
            }
            saveDefaults(module.getServerLanguage());
        }

    }

    public void loadLanguage(String language) {

        File f = new File(folder, language + provider.getFileExtension());

        ConfigSection sec;
        if(f.exists()) {
            sec = provider.loadFromFile(f);
        } else {
            sec = new ConfigSection();
        }

        HashMap<String, String> ents = loadEntries(sec);

        if (addDefaults(ents)) {

            saveEntries(language, ents);
        }

        entries.put(language, ents);

    }

    private boolean addDefaults(HashMap<String, String> sec) {

        boolean out = false;
        for(Map.Entry<String, String> ent : defaults.entrySet()) {

            if(!sec.containsKey(ent.getKey())) {
                out = true;
                sec.put(ent.getKey(), ent.getValue());
            }
        }

        return out;
    }

    private HashMap<String, String> loadEntries(ConfigSection sec) {

        return populateMap(sec, "");
    }

    private HashMap<String, String> populateMap(ConfigSection section, String prefix) {

        HashMap<String, String> out = new HashMap<>();
        for(String s : section.getKeys()) {
            if(section.has(s, String.class)) {
                out.put(prefix + s, section.getString(s));
            } else if(section.has(s, ConfigSection.class)) {
                out.putAll(populateMap(section.getSection(s), prefix + s + "."));
            }
        }

        return out;
    }

    @Override
    public MComponent getMessage(String key, String language, Object... args) {

        String msg = getRawMessage(key, language);
        if(msg == null) return MComponent.createTextComponent("");

        msg = module.applyInlinePlaceholders(msg, args);

        MComponent out = MComponent.Serializer.parse(msg);
        out = module.applyPlaceholders(out, args);

        return out;
    }

    @Override
    public MComponent getUnformattedMessage(String key, String language) {

        String msg = getRawMessage(key, language);
        if(msg == null) return MComponent.createTextComponent("");

        return MComponent.Serializer.parse(msg);
    }

    @Override
    public String getRawMessage(String key, String language) {

        if(language == null) return defaults.get(key);

        if(!entries.containsKey(language)) {
            loadLanguage(language);
        }

        if(entries.containsKey(language) && entries.get(language).containsKey(key)) {
            return entries.get(language).get(key);
        }
        return defaults.get(key);
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
    public void reloadAllEntries() {

        Set<String> langs = entries.keySet();
        entries.clear();

        for(String lang : langs) {
            loadLanguage(lang);
        }
    }

    @Override
    public boolean hasKey(String key) {
        return entries.get(module.getServerLanguage()).containsKey(key) || defaults.containsKey(key);
    }

    @Override
    public boolean hasKey(String key, String language) {

        if(language == null || !entries.containsKey(language)) return hasKey(key);
        return entries.get(language).containsKey(key) || hasKey(key);
    }

    @Override
    public boolean hasKey(String key, MPlayer player) {

        String language = module.getPlayerLocale(player);
        return hasKey(key, language);
    }

    @Override
    public void saveDefaults(String file) {

        saveEntries(file, defaults);
    }

    public void saveEntries(String file, HashMap<String, String> entries) {

        File f = new File(folder, file + provider.getFileExtension());

        ConfigSection sec = new ConfigSection();
        for(Map.Entry<String, String> ent : entries.entrySet()) {

            sec.set(ent.getKey(), ent.getValue());
        }

        provider.saveToFile(sec, f);
    }

    @Override
    public void sendMessage(String key, MPlayer player, Object... args) {

        MComponent message = getMessage(key, player, args);
        player.sendMessage(message);

    }

    @Override
    public void sendMessage(String key, Iterable<MPlayer> players, Object... args) {

        HashMap<String, MComponent> cachedMessages = new HashMap<>();
        for (MPlayer u : players) {

            String lang = module.getPlayerLocale(u);
            MComponent message = cachedMessages.computeIfAbsent(lang, k -> getMessage(key, lang, args));

            u.sendMessage(message);
        }
    }

    @Override
    public void sendTitle(String key, MPlayer player, MTitle.TitleOptions opts, Object... args) {

        MTitle title = new MTitle(getMessage(key, player, args), opts);
        player.sendTitle(title);

    }

    @Override
    public void sendTitle(String key, Iterable<MPlayer> players, MTitle.TitleOptions opts, Object... args) {

        HashMap<String, MTitle> cachedMessages = new HashMap<>();
        for(MPlayer u : players) {

            String lang = module.getPlayerLocale(u);
            MTitle title = cachedMessages.computeIfAbsent(lang, k -> new MTitle(getMessage(key, lang, args), opts));

            u.sendTitle(title);
        }
    }

    @Override
    public void sendActionBar(String key, MPlayer player, MActionBar.ActionBarOptions opts, Object... args) {

        MActionBar ab = new MActionBar(getMessage(key, player, args), opts);
        player.sendActionBar(ab);

    }

    @Override
    public void sendActionBar(String key, Iterable<MPlayer> players, MActionBar.ActionBarOptions opts, Object... args) {

        HashMap<String, MActionBar> cachedMessages = new HashMap<>();
        for(MPlayer u : players) {

            String lang = module.getPlayerLocale(u);
            MActionBar ab = cachedMessages.computeIfAbsent(lang, k -> new MActionBar(getMessage(key, lang, args), opts));

            u.sendActionBar(ab);
        }
    }

    @Override
    public ILangModule getModule() {
        return module;
    }
}
