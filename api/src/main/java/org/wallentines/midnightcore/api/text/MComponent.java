package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.config.serialization.InlineSerializer;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class MComponent {

    protected ComponentType type;
    protected String content;
    protected String insertion;
    protected MStyle style;

    protected MHoverEvent hoverEvent;
    protected MClickEvent clickEvent;

    protected List<MComponent> children = new ArrayList<>();

    protected MComponent(ComponentType type, String content) {
        this.type = type;
        this.content = content;
        this.style = new MStyle();
    }

    public String getContent() {
        return content;
    }

    public String getAllContent() {

        StringBuilder builder = new StringBuilder(content);
        for(MComponent child : children) {
            builder.append(child.getAllContent());
        }

        return builder.toString();
    }

    public MStyle getStyle() {
        return style;
    }

    public MComponent withStyle(MStyle style) {
        this.style = style;
        return this;
    }

    public Iterable<MComponent> getChildren() {
        return children;
    }

    public void addChild(MComponent comp) {
        children.add(comp);
    }

    public MComponent withChild(MComponent comp) {
        children.add(comp);
        return this;
    }

    public MHoverEvent getHoverEvent() {
        return hoverEvent;
    }

    public void setHoverEvent(MHoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
    }

    public MComponent withHoverEvent(MHoverEvent hoverEvent) {
        this.hoverEvent = hoverEvent;
        return this;
    }

    public MClickEvent getClickEvent() {
        return clickEvent;
    }

    public void setClickEvent(MClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    public MComponent withClickEvent(MClickEvent clickEvent) {
        this.clickEvent = clickEvent;
        return this;
    }

    public String getInsertion() {
        return insertion;
    }

    public MComponent copy() {
        MComponent out = baseCopy().withStyle(style);
        out.setHoverEvent(hoverEvent);
        out.setClickEvent(clickEvent);
        out.insertion = insertion;

        for(MComponent comp : children) {
            out.addChild(comp.copy());
        }
        return out;
    }

    public String toLegacyText() {
        return toPlainText('\u00A7', null);
    }

    public String toConfigText() {

        if(hasNonLegacyComponents()) {
            return toString();
        }

        return toPlainText('&', MidnightCoreAPI.getInstance().getGameVersion().getMinorVersion() > 15 ? '#' : null);
    }

    private String toPlainText(Character colorChar, Character hexChar) {

        StringBuilder out = new StringBuilder(style.toLegacyStyle(colorChar, hexChar)).append(content);

        for(MComponent comp : children) {
            out.append(comp.toPlainText(colorChar, hexChar));
        }

        return out.toString();
    }

    @Override
    public String toString() {

        return SERIALIZER.serialize(this).toString();
    }

    protected abstract MComponent baseCopy();

    private boolean hasNonLegacyComponents() {
        return style.getFont() != null || hoverEvent != null || clickEvent != null || insertion != null;
    }

    public static MComponent parse(String s) {

        if(s.startsWith("{")) {

            try {
                ConfigSection sec = JsonConfigProvider.INSTANCE.loadFromString(s);
                MComponent comp = SERIALIZER.deserialize(sec);
                if(comp != null) return comp;

            } catch (Exception ex) {
                // Ignore
            }
        }

        return parsePlainText(s);
    }

    private static MComponent parsePlainText(String content) {

        MComponent out = new MTextComponent("");

        StringBuilder currentString = new StringBuilder();
        MStyle currentStyle = new MStyle();

        for(int i = 0 ; i < content.length() ; i++) {

            char c = content.charAt(i);
            if(c == '&' && i < content.length() - 1) {
                char next = content.charAt(i + 1);

                if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')) {

                    int legacy = Integer.parseInt(next + "", 16);
                    if(currentString.length() > 0) {
                        MComponent comp = new MTextComponent(currentString.toString()).withStyle(currentStyle);

                        out.addChild(comp);

                    }
                    currentString = new StringBuilder();

                    i += 1;
                    currentStyle = new MStyle().withColor(TextColor.fromRGBI(legacy));
                }
                switch (next) {
                    case 'l': { currentStyle.withBold(true); i += 1; break; }
                    case 'o': { currentStyle.withItalic(true); i += 1; break; }
                    case 'n': { currentStyle.withUnderlined(true); i += 1; break; }
                    case 'm': { currentStyle.withStrikethrough(true); i += 1; break; }
                    case 'k': { currentStyle.withObfuscated(true); i += 1; break; }
                    case 'r': { currentStyle.withReset(true); i += 1; break; }
                }

            } else if(c == '#' && i < content.length() - 7) {

                String hex = content.substring(i+1, i+7);
                MComponent comp = new MTextComponent(currentString.toString()).withStyle(currentStyle);
                out.addChild(comp);

                currentString = new StringBuilder();

                i += 6;
                currentStyle = new MStyle().withColor(new TextColor(hex));

            } else {
                currentString.append(c);
            }
        }

        MComponent comp = new MTextComponent(currentString.toString()).withStyle(currentStyle);
        out.addChild(comp);

        return out;
    }

    // Protected Methods
    protected abstract void onSerialize(ConfigSection sec);

    // Static Fields
    public static final ConfigSerializer<MComponent> SERIALIZER = new ConfigSerializer<MComponent>() {

        @Override
        public MComponent deserialize(ConfigSection section) {

            ComponentType type = null;
            for(String s : section.getKeys()) {
                ComponentType ct = ComponentType.getById(s);
                if(ct != null) {
                    type = ct;
                    break;
                }
            }

            if(type == null) return null;

            MComponent out = type.deserialize(section).withStyle(MStyle.SERIALIZER.deserialize(section));

            if(section.has("clickEvent")) out.clickEvent = section.get("clickEvent", MClickEvent.class);
            if(section.has("hoverEvent")) out.hoverEvent = section.get("hoverEvent", MHoverEvent.class);

            if(section.has("insertion", String.class)) out.insertion = section.getString("insertion");
            if(section.has("extra", List.class)) {
                out.children.addAll(section.getList("extra", MComponent.class));
            }

            return out;
        }

        @Override
        public ConfigSection serialize(MComponent object) {

            ConfigSection sec = new ConfigSection()
                    .with(object.type.getId(), object.content);

            if(object.style != null) sec.fill(MStyle.SERIALIZER.serialize(object.style));
            if(object.clickEvent != null) sec.set("clickEvent", object.clickEvent);
            if(object.hoverEvent != null) sec.set("hoverEvent", object.hoverEvent);

            object.onSerialize(sec);

            List<ConfigSection> extra = new ArrayList<>();
            for(MComponent comp : object.children) {
                extra.add(serialize(comp));
            }
            if(!extra.isEmpty()) sec.set("extra", extra);
            if(object.insertion != null) sec.set("insertion", object.insertion);

            return sec;
        }
    };

    public static final InlineSerializer<MComponent> INLINE_SERIALIZER = InlineSerializer.of(MComponent::toConfigText, MComponent::parse);

    protected enum ComponentType {

        TEXT("text", sec -> new MTextComponent(sec.getString("text"))),
        TRANSLATE("translate", sec -> new MTranslateComponent(sec.getString("translate"), sec.has("with", List.class) ? sec.getListFiltered("with", MComponent.class) : null));

        final String id;
        final Function<ConfigSection, MComponent> deserializer;

        ComponentType(String id, Function<ConfigSection, MComponent> deserializer) {
            this.id = id;
            this.deserializer = deserializer;
        }

        String getId() {
            return id;
        }

        MComponent deserialize(ConfigSection section) {
            return deserializer.apply(section);
        }

        static ComponentType getById(String id) {
            for(ComponentType type : values()) {
                if(type.id.equals(id)) return type;
            }
            return null;
        }
    }
}
