package me.m1dnightninja.midnightcore.common.module.lang;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract  class AbstractLangProvider implements ILangProvider {

    protected final HashMap<String, HashMap<String, String>> entries = new HashMap<>();

    protected final File folder;
    protected final ConfigProvider provider;
    protected final ILangModule module;

    protected final HashMap<String, String> defaults;

    protected AbstractLangProvider(File folder, ILangModule mod, ConfigProvider provider, ConfigSection defaultEntries) {

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

        if(!f.exists()) {
            return;
        }

        ConfigSection sec = provider.loadFromFile(f);
        entries.put(language, loadEntries(sec));

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

        msg = applyInlinePlaceholders(msg, module, args);

        MComponent out = MComponent.Serializer.parse(msg);
        out = applyPlaceholders(out, module, args);

        out.format(args);

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
    public MComponent getMessage(String key, UUID player, Object... args) {
        return getMessage(key, module.getPlayerLocale(player), args);
    }

    @Override
    public MComponent getMessage(String key) {
        return getMessage(key, module.getServerLanguage());
    }

    public static String applyInlinePlaceholders(String msg, ILangModule module, Object... args) {

        boolean placeholder = false;
        StringBuilder currentPlaceholder = new StringBuilder();
        StringBuilder message = new StringBuilder();

        for(int i = 0 ; i < msg.length() ; i++) {

            char c = msg.charAt(i);
            if(c == '%') {

                if(placeholder) {

                    String rep = module.getInlinePlaceholderValue(currentPlaceholder.toString(), args);
                    message.append(rep == null ? "%" + currentPlaceholder.toString() + "%" : rep);

                    currentPlaceholder = new StringBuilder();

                }

                placeholder = !placeholder;

            } else {
                if(placeholder) {
                    currentPlaceholder.append(c);
                } else {
                    message.append(c);
                }
            }
        }

        if(!currentPlaceholder.isEmpty()) message.append("%").append(currentPlaceholder);
        return message.toString();
    }

    public static MComponent applyPlaceholders(MComponent msg, ILangModule module, Object... args) {

        MStyle style = msg.getStyle();
        MComponent out = MComponent.createTextComponent("").withStyle(style);

        boolean placeholder = false;
        StringBuilder currentPlaceholder = new StringBuilder();
        StringBuilder currentMessage = new StringBuilder();

        for(int i = 0 ; i < msg.getContent().length() ; i++) {

            char c = msg.getContent().charAt(i);

            if(c == '%') {

                if(placeholder) {

                    MComponent rep = module.getPlaceholderValue(currentPlaceholder.toString(), args);
                    if(rep == null) {
                        currentMessage.append("%").append(currentPlaceholder).append("%");

                    } else {

                        out.addChild(MComponent.createTextComponent(currentMessage.toString()));
                        out.addChild(rep);

                        currentMessage = new StringBuilder();
                    }

                    currentPlaceholder = new StringBuilder();
                }

                placeholder = !placeholder;

            } else {
                if(placeholder) {
                    currentPlaceholder.append(c);
                } else {
                    currentMessage.append(c);
                }
            }
        }

        if(!currentPlaceholder.isEmpty()) currentMessage.append("%").append(currentPlaceholder);
        out.addChild(MComponent.createTextComponent(currentMessage.toString()));

        for(MComponent comp : msg.getChildren()) {
            out.addChild(applyPlaceholders(comp, module, args));
        }

        return out;
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
    public boolean hasKey(String key, UUID player) {

        String language = module.getPlayerLocale(player);
        return hasKey(key, language);
    }

    @Override
    public void saveDefaults(String file) {
        File f = new File(folder, file + provider.getFileExtension());

        ConfigSection sec = new ConfigSection();
        for(Map.Entry<String, String> ent : defaults.entrySet()) {

            sec.set(ent.getKey(), ent.getValue());
        }

        provider.saveToFile(sec, f);
    }
}
