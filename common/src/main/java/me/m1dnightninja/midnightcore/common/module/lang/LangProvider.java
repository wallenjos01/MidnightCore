package me.m1dnightninja.midnightcore.common.module.lang;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class LangProvider implements ILangProvider {

    private final HashMap<String, HashMap<String, String>> entries = new HashMap<>();

    private final File folder;
    private final ConfigProvider provider;
    private final ILangModule module;

    private final HashMap<String, String> defaults;

    public LangProvider(File folder, ILangModule mod, ConfigProvider provider, ConfigSection defaultEntries) {

        this.folder = folder;
        this.provider = provider;
        this.module = mod;

        defaults = loadEntries(defaultEntries);

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

        HashMap<String, String> out = new HashMap<>();
        for(String s : sec.getKeys()) {
            out.put(s, sec.get(s, String.class));
        }

        return out;
    }

    @Override
    public MComponent getMessage(String key, String language, Object... args) {

        String msg = getRawMessage(key, language);
        msg = applyInlinePlaceholders(msg, args);

        MComponent out = MComponent.Serializer.parse(msg);
        out = applyPlaceholders(out, args);

        return out;
    }

    @Override
    public MComponent getUnformattedMessage(String key, String language) {
        String msg = getRawMessage(key, language);
        return MComponent.Serializer.parse(msg);
    }

    @Override
    public String getRawMessage(String key, String language) {

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

    private String applyInlinePlaceholders(String msg, Object... args) {

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
                continue;

            } else {
                if(placeholder) {
                    currentPlaceholder.append(c);
                } else {
                    message.append(c);
                }
            }

            message.append(c);
        }

        message.append(currentPlaceholder);
        return message.toString();
    }

    private MComponent applyPlaceholders(MComponent msg, Object... args) {

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
                continue;

            } else {
                if(placeholder) {
                    currentPlaceholder.append(c);
                } else {
                    currentMessage.append(c);
                }
            }

            currentMessage.append(c);
        }

        currentMessage.append(currentPlaceholder);
        out.addChild(MComponent.createTextComponent(currentMessage.toString()));

        for(MComponent comp : msg.getChildren()) {
            out.addChild(applyPlaceholders(comp, args));
        }

        return out;
    }
}
