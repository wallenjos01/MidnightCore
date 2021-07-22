package me.m1dnightninja.midnightcore.api.text;

import com.google.gson.*;
import me.m1dnightninja.midnightcore.api.math.Color;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MComponent {

    protected Type type;
    protected String content;
    protected MStyle style;

    protected MHoverEvent hoverEvent;
    protected MClickEvent clickEvent;

    protected List<MComponent> data = new ArrayList<>();
    protected List<MComponent> children = new ArrayList<>();

    protected MComponent(Type type, String content) {
        this.type = type;
        this.content = content;
        this.style = new MStyle();
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public MStyle getStyle() {
        return style;
    }

    public List<MComponent> getChildren() {
        return children;
    }

    public List<MComponent> getAllChildren() {

        List<MComponent> cmp = new ArrayList<>(children);
        for(MComponent comp : children) {
            cmp.addAll(comp.getAllChildren());
        }

        return cmp;
    }


    public MComponent withStyle(MStyle style) {
        this.style = style;
        return this;
    }


    public MComponent addChild(MComponent other) {

        if(findChild(other, this)) {
            throw new IllegalArgumentException("Cyclical component inheritance detected!");
        }

        children.add(other);
        return this;
    }

    public void clearStyle() {

        setStyle(new MStyle());
        for(MComponent cmp : children) {
            cmp.clearStyle();
        }
    }

    public List<MComponent> getTranslateData() {
        return data;
    }

    public static MComponent createTextComponent(String content) {
        return new MComponent(Type.TEXT, content);
    }

    public static MComponent createTranslatableComponent(String content, MComponent... values) {

        MComponent out = new MComponent(Type.TRANSLATABLE, content);
        out.data = Arrays.asList(values);

        return out;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStyle(MStyle style) {
        this.style = style;
    }

    public String toLegacyText(boolean hexSupport) {

        StringBuilder out = new StringBuilder();
        out.append(style.toLegacyText(hexSupport)).append(content);

        for(MComponent cmp : children) {
            out.append(cmp.toLegacyText(hexSupport));
        }

        return out.toString();

    }

    private boolean findChild(MComponent search, MComponent child) {

        if(search == child) return true;

        for(MComponent cmp : search.children) {
            if(findChild(cmp, child)) return true;
        }

        return false;
    }

    public MComponent withHoverEvent(MHoverEvent event) {
        this.hoverEvent = event;
        return this;
    }

    public MComponent copy() {

        MComponent out = new MComponent(type, content);
        out.setStyle(style.copy());

        for(MComponent cmp : children) {
            out.addChild(cmp.copy());
        }

        return out;
    }

    public MComponent plainCopy() {

        MComponent out = new MComponent(type, content);
        for(MComponent cmp : children) {
            out.addChild(cmp.plainCopy());
        }

        return out;
    }

    public String allContent() {

        StringBuilder current = new StringBuilder();
        current.append(getContent());
        for(MComponent cmp : children) {
            current.append(cmp.allContent());
        }

        return current.toString();
    }

    public MHoverEvent getHoverEvent() {
        return hoverEvent;
    }

    public MClickEvent getClickEvent() {
        return clickEvent;
    }

    public void send(MPlayer u) {
        u.sendMessage(this);
    }

    @Override
    public String toString() {
        return allContent();
    }

    public enum Type {

        TEXT("text"),
        TRANSLATABLE("translate");

        private final String id;

        Type(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Type getById(String id) {
            for(Type type : values()) {
                if(id.equals(type.id)) return type;
            }
            return null;
        }

    }

    public static class Serializer {

        private static final Gson GSON = new GsonBuilder().create();

        public static MComponent fromJson(String json) {

            JsonObject obj = GSON.fromJson(json, JsonObject.class);
            return fromJson(obj);
        }

        public static MComponent fromJson(JsonObject json) {

            MComponent.Type cType = null;
            String content = "";

            for(MComponent.Type type : MComponent.Type.values()) {
                if(json.has(type.getId())) {
                    cType = type;
                    content = json.get(type.getId()).getAsString();
                    break;
                }
            }

            if(cType == null) {
                cType = MComponent.Type.TEXT;
            }

            MStyle style = styleFromJson(json);
            MComponent out;

            if(cType == MComponent.Type.TRANSLATABLE) {

                if(json.has("with") && json.get("with").isJsonArray()) {

                    JsonArray arr = json.get("with").getAsJsonArray();
                    MComponent[] data = new MComponent[arr.size()];

                    for(int i = 0 ; i < arr.size() ; i++) {
                        JsonElement ele = arr.get(i);
                        if(!ele.isJsonObject()) continue;

                        data[i] = fromJson(ele.getAsJsonObject());
                    }

                    out = MComponent.createTranslatableComponent(content, data).withStyle(style);

                } else {

                    out = MComponent.createTranslatableComponent(content).withStyle(style);
                }

            } else {

                out = MComponent.createTextComponent(content).withStyle(style);
            }

            if(json.has("hoverEvent") && json.get("hoverEvent").isJsonObject()) {

                out.withHoverEvent(MHoverEvent.fromJson(json.get("hoverEvent").getAsJsonObject()));
            }

            if(json.has("extra") && json.get("extra").isJsonArray()) {

                for(JsonElement ele : json.get("extra").getAsJsonArray()) {

                    if(!ele.isJsonObject()) continue;
                    out.addChild(fromJson(ele.getAsJsonObject()));
                }
            }

            return out;
        }

        public static JsonObject toJson(MComponent component) {

            JsonObject out = new JsonObject();
            fillJson(out, component.style);

            out.addProperty(component.getType().getId(), component.content);

            if(component.hoverEvent != null) {
                out.add("hoverEvent", component.hoverEvent.toJson());
            }

            if(component.data.size() > 0) {
                JsonArray arr = new JsonArray();
                for(MComponent cmp : component.data) {
                    arr.add(toJson(cmp));
                }
                out.add("with", arr);
            }
            if(component.children.size() > 0) {
                JsonArray arr = new JsonArray();
                for(MComponent cmp : component.children) {
                    arr.add(toJson(cmp));
                }
                out.add("extra", arr);
            }

            return out;
        }

        public static String toJsonString(MComponent component) {
            JsonObject obj = toJson(component);
            return GSON.toJson(obj);
        }

        public static MStyle styleFromJson(JsonObject json) {

            MStyle out = new MStyle();

            if(json.has("color")) {
                JsonElement ele = json.get("color");
                out.withColor(Color.parse(ele.getAsString()));
            }

            if(json.has("bold")) {
                JsonElement ele = json.get("bold");
                out.withBold(ele.getAsBoolean());
            }

            if(json.has("italic")) {
                JsonElement ele = json.get("italic");
                out.withItalic(ele.getAsBoolean());
            }

            if(json.has("underline")) {
                JsonElement ele = json.get("underline");
                out.withUnderline(ele.getAsBoolean());
            }

            if(json.has("strikethrough")) {
                JsonElement ele = json.get("strikethrough");
                out.withStrikethrough(ele.getAsBoolean());
            }

            if(json.has("obfuscated")) {
                JsonElement ele = json.get("obfuscated");
                out.withObfuscated(ele.getAsBoolean());
            }

            if(json.has("font")) {
                JsonElement ele = json.get("font");
                out.withFont(MIdentifier.parse(ele.getAsString()));
            }

            return out;
        }

        public static void fillJson(JsonObject obj, MStyle style) {

            if(style.getColor() != null) {
                obj.addProperty("color", style.getColor().toHex());
            }
            if(style.getFont() != null) {
                obj.addProperty("font", style.getFont().toString());
            }
            if(style.isBold() != null) {
                obj.addProperty("bold", style.isBold());
            }
            if(style.isItalic() != null) {
                obj.addProperty("italic", style.isItalic());
            }
            if(style.isUnderlined() != null) {
                obj.addProperty("underlined", style.isUnderlined());
            }
            if(style.isStrikethrough() != null) {
                obj.addProperty("strikethrough", style.isStrikethrough());
            }
            if(style.isObfuscated() != null) {
                obj.addProperty("obfuscated", style.isObfuscated());
            }
        }

        public static MComponent parse(String s) {

            if(!s.startsWith("{")) {
                return parseLegacyText(s, '&', '#');
            }

            try {
                JsonObject obj = GSON.fromJson(s, JsonObject.class);
                return fromJson(obj);

            } catch(Exception ex) {

                return parseLegacyText(s, '&', '#');
            }
        }

        public static String toLegacyText(MComponent component, boolean hexSupport) {
            return component.toLegacyText(hexSupport);
        }

        public static String serialize(MComponent component) {

            if(component.style.getFont() != null || component.hoverEvent != null || component.clickEvent != null) {
                return toJsonString(component);
            }

            return toLegacyText(component, true);

        }

        public static MComponent parseLegacyText(String content, Character legacyColorCharacter, Character rgbColorCharacter) {

            MComponent out = new MComponent(Type.TEXT, "");

            StringBuilder currentString = new StringBuilder();
            MStyle currentStyle = new MStyle();

            for(int i = 0 ; i < content.length() ; i++) {

                char c = content.charAt(i);
                if(c == legacyColorCharacter && i < content.length() - 1) {
                    char next = content.charAt(i + 1);

                    if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')) {

                        int rgbi = Integer.parseInt(next + "", 16);

                        if(currentString.length() > 0) out.addChild(createTextComponent(currentString.toString()).withStyle(currentStyle));
                        currentString = new StringBuilder();

                        i += 1;

                        currentStyle = new MStyle().withColor(Color.fromRGBI(rgbi));
                    }
                    switch (next) {
                        case 'l' -> {
                            currentStyle.withBold(true);
                            i += 1;
                        }
                        case 'o' -> {
                            currentStyle.withItalic(true);
                            i += 1;
                        }
                        case 'n' -> {
                            currentStyle.withUnderline(true);
                            i += 1;
                        }
                        case 'm' -> {
                            currentStyle.withStrikethrough(true);
                            i += 1;
                        }
                        case 'k' -> {
                            currentStyle.withObfuscated(true);
                            i += 1;
                        }
                    }

                } else if(c == rgbColorCharacter && i < content.length() - 7) {

                    String hex = content.substring(i+1, i+7);

                    out.addChild(createTextComponent(currentString.toString()).withStyle(currentStyle));
                    currentString = new StringBuilder();

                    i += 6;

                    currentStyle = new MStyle().withColor(new Color(hex));

                } else {

                    currentString.append(c);
                }
            }

            out.addChild(createTextComponent(currentString.toString()).withStyle(currentStyle));

            return out;

        }

    }
}
