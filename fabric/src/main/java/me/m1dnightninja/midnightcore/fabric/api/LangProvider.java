package me.m1dnightninja.midnightcore.fabric.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.m1dnightninja.midnightcore.api.lang.AbstractLangProvider;
import me.m1dnightninja.midnightcore.fabric.module.LangModule;
import me.m1dnightninja.midnightcore.fabric.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LangProvider extends AbstractLangProvider {

    private final LangModule mod;

    public LangProvider(File folder, LangModule mod) {
        super(folder);
        this.mod = mod;
    }

    @Override
    public String getRawMessage(String language, String key) {
        return entries.get(language).get(key);
    }

    @Override
    public String getMessage(String language, String key, Object... objs) {
        return Component.Serializer.toJson(formatMessage(entries.get(language).get(key), objs));
    }

    public Component getMessageAsComponent(String language, String key, Object... objs) {
        return formatMessage(entries.get(language).get(key), objs);
    }

    @Override
    protected boolean verifyFile(File f) {
        return JsonConfiguration.loadFromFile(f) != null;
    }

    @Override
    protected void loadEntries(String fileName) {

        File f = files.get(fileName);
        if(f == null) return;

        JsonConfiguration conf = JsonConfiguration.loadFromFile(f);
        if(conf == null) return;

        JsonObject obj = conf.getRoot();

        HashMap<String, String> ents = new HashMap<>();

        for(Map.Entry<String, JsonElement> ent : obj.entrySet()) {
            ents.put(ent.getKey(), ent.getValue().getAsString());
        }

        entries.put(fileName, ents);
    }

    public Component formatMessage(String message, Object... args) {

        // Scan for placeholders before converting to component

        StringBuilder currentLiteral = new StringBuilder();
        StringBuilder currentPlaceholder = new StringBuilder();

        int placeholderStart = -1;

        for(int i = 0 ; i < message.length() ; i++) {
            char c = message.charAt(i);

            if(c == '%') {

                if(placeholderStart == -1) {
                    placeholderStart = i;
                } else {
                    placeholderStart = -1;
                    currentLiteral.append(mod.getStringPlaceholderValue(currentPlaceholder.toString(), args));
                    currentPlaceholder = new StringBuilder();
                }

            } else {

                if (placeholderStart == -1) {
                    currentLiteral.append(c);
                } else {
                    currentPlaceholder.append(c);
                }
            }

        }
        currentLiteral.append(currentPlaceholder);
        message = currentLiteral.toString();

        currentLiteral = new StringBuilder();
        currentPlaceholder = new StringBuilder();

        MutableComponent orig = TextUtil.parse(message);

        String msg = orig.getContents();
        Style style = orig.getStyle();

        List<Component> texts = new ArrayList<>();

        for(int i = 0 ; i < msg.length() ; i++) {

            char c = message.charAt(i);

            if(c == '%') {

                if(placeholderStart == -1) {
                    placeholderStart = i;
                } else {
                    placeholderStart = -1;
                    texts.add(new TextComponent(currentLiteral.toString()));
                    texts.add(mod.getRawPlaceholderValue(currentPlaceholder.toString(), args));

                    currentLiteral = new StringBuilder();
                    currentPlaceholder = new StringBuilder();
                }

            } else {

                if(placeholderStart == -1) {
                    currentPlaceholder.append(c);
                } else {
                    currentLiteral.append(c);
                }

            }
        }

        currentLiteral.append(currentPlaceholder);

        TextComponent out = new TextComponent("");
        out.setStyle(style);

        for(Component c : texts) {
            out.append(c);
        }

        return out;
    }
}
