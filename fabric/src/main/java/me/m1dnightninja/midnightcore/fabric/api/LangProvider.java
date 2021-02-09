package me.m1dnightninja.midnightcore.fabric.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.lang.AbstractLangProvider;
import me.m1dnightninja.midnightcore.common.JsonWrapper;
import me.m1dnightninja.midnightcore.fabric.module.LangModule;
import me.m1dnightninja.midnightcore.fabric.util.TextUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LangProvider extends AbstractLangProvider {

    private final LangModule mod;

    public LangProvider(File folder, LangModule mod, HashMap<String, String> defaults) {
        super(folder, defaults);
        this.mod = mod;
    }

    @Override
    public boolean hasMessage(String language, String key) {

        return getRawMessage(language, key) != null;
    }

    @Override
    public String getRawMessage(String language, String key) {

        if(!language.endsWith(".json")) language += ".json";
        if(!entries.containsKey(language)) return key;

        return entries.get(language).get(key);
    }

    @Override
    public String getMessage(String language, String key, Object... objs) {
        return Component.Serializer.toJson(formatMessage(getRawMessage(language, key), objs));
    }

    public Component getMessageAsComponent(String key, Object... objs) {
        return getMessageAsComponent(getServerLanguage(), key, objs);
    }

    public Component getMessageAsComponent(String language, String key, Object... objs) {
        return formatMessage(getRawMessage(language, key), objs);
    }

    @Override
    protected boolean verifyFile(File f) {
        return JsonWrapper.loadFromFile(f) != null;
    }

    @Override
    protected void loadEntries(String fileName) {

        File f = files.get(fileName);
        if(f == null) return;

        JsonWrapper conf = JsonWrapper.loadFromFile(f);
        if(conf == null) {
            MidnightCoreAPI.getLogger().warn("Unable to load entries from file " + fileName);
            return;
        }

        JsonObject obj = conf.getRoot();

        HashMap<String, String> ents = new HashMap<>(defaults);

        for(Map.Entry<String, JsonElement> ent : obj.entrySet()) {
            ents.put(ent.getKey(), ent.getValue().getAsString());
        }

        entries.put(fileName, ents);

        for(Map.Entry<String, String> ent : ents.entrySet()) {
            obj.addProperty(ent.getKey(), ent.getValue());
        }
        conf.save(f);
    }

    @Override
    public String getServerLanguage() {
        return "en_us";
    }

    public Component formatMessage(String message, Object... args) {

        if(message == null) return new TextComponent(" ");

        // Scan for placeholders before converting to component

        StringBuilder currentLiteral = new StringBuilder();
        StringBuilder currentPlaceholder = new StringBuilder();

        boolean placeholderStarted = false;

        for(int i = 0 ; i < message.length() ; i++) {
            char c = message.charAt(i);

            if(c == '%') {

                if(placeholderStarted) {
                    placeholderStarted = false;
                    currentLiteral.append(mod.getStringPlaceholderValue(currentPlaceholder.toString(), args));
                    currentPlaceholder = new StringBuilder();
                } else {
                    placeholderStarted = true;
                }

            } else {

                if (placeholderStarted) {
                    currentPlaceholder.append(c);
                } else {
                    currentLiteral.append(c);
                }
            }

        }
        currentLiteral.append(currentPlaceholder);
        message = currentLiteral.toString();

        currentLiteral = new StringBuilder();
        currentPlaceholder = new StringBuilder();
        placeholderStarted = false;

        MutableComponent orig = TextUtil.parse(message);

        String msg = orig.getContents();

        TextComponent out = new TextComponent("");
        out.setStyle(orig.getStyle());

        List<Component> texts = new ArrayList<>();

        for(int i = 0 ; i < msg.length() ; i++) {

            char c = msg.charAt(i);

            if(c == '%') {

                if(placeholderStarted) {
                    placeholderStarted = false;
                    texts.add(new TextComponent(currentLiteral.toString()));
                    texts.add(mod.getRawPlaceholderValue(currentPlaceholder.toString(), args));

                    currentLiteral = new StringBuilder();
                    currentPlaceholder = new StringBuilder();
                } else {
                    placeholderStarted = true;
                }

            } else {

                if(placeholderStarted) {
                    currentPlaceholder.append(c);
                } else {
                    currentLiteral.append(c);
                }

            }
        }

        currentLiteral.append(currentPlaceholder);
        texts.add(new TextComponent(currentLiteral.toString()));

        for(Component c : texts) {
            out.append(c);
        }

        return out;
    }
}
