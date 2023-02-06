package org.wallentines.midnightcore.api.text;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightcore.api.MidnightCoreAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("unused")
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
        if(comp == null) return;
        children.add(comp);
    }

    public MComponent withChild(MComponent comp) {
        addChild(comp);
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

        return toPlainText('&', getGameVersion() > 15 ? '#' : null);
    }

    public String toItemText() {

        if(getGameVersion() > 12) {
            return toJSONString();
        }

        return toLegacyText();
    }

    private String toPlainText(Character colorChar, Character hexChar) {

        StringBuilder out = new StringBuilder(style.toLegacyStyle(colorChar, hexChar)).append(content);

        for(MComponent comp : children) {
            out.append(comp.toPlainText(colorChar, hexChar));
        }

        return out.toString();
    }

    public int getLength() {

        int out = contentLength();
        for(MComponent comp : children) {
            out += comp.getLength();
        }

        return out;
    }

    public MComponent subComponent(int beginIndex, int endIndex) {

        int length = getLength();
        if(beginIndex < 0 || endIndex >= length) throw new IllegalStateException("Requested sub-component with bounds (" + beginIndex + "," + endIndex + ") exceeds component bounds!");

        int index = contentLength();

        MComponent out = baseCopy().withStyle(style);
        if(index > endIndex) {

            out.content = out.content.substring(beginIndex, endIndex);
        }

        for(MComponent comp : children) {

            out.addChild(comp.subComponent(Math.max(beginIndex - index, 0), endIndex - index));
        }

        return out;
    }

    public String toJSONString() {
        return JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, OBJECT_SERIALIZER.serialize(ConfigContext.INSTANCE, this).getOrThrow());
    }

    @Override
    public String toString() {
        return "MComponent{" +
                "type=" + type +
                ", content='" + content + '\'' +
                '}';
    }

    protected abstract MComponent baseCopy();

    protected abstract int contentLength();

    private boolean hasNonLegacyComponents() {
        return style.getFont() != null || hoverEvent != null || clickEvent != null || insertion != null;
    }

    public static MComponent parse(String s) {

        if(s.isEmpty()) return new MTextComponent("");
        if(s.stripLeading().charAt(0) == '{') {
            try {
                return parseJSON(s);
            } catch (DecodeException ex) {
                // Ignore
            }
        }

        return parsePlainText(s);
    }

    private static int getGameVersion() {
        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) return 19;

        return api.getGameVersion().getMinorVersion();
    }

    public static MComponent parseJSON(String s) throws DecodeException {

        JSONCodec codec = JSONCodec.minified();
        ConfigObject obj = codec.decode(ConfigContext.INSTANCE, s);

        return OBJECT_SERIALIZER.deserialize(ConfigContext.INSTANCE, obj).getOrThrow();
    }

    private static MComponent parsePlainText(String content) {

        List<MComponent> out = new ArrayList<>();

        StringBuilder currentString = new StringBuilder();
        MStyle currentStyle = new MStyle();

        boolean reset = false;

        for(int i = 0 ; i < content.length() ; i++) {

            char c = content.charAt(i);
            if(c == '&' && i < content.length() - 1) {
                char next = content.charAt(i + 1);

                if ((next >= '0' && next <= '9') || (next >= 'a' && next <= 'f')) {

                    int legacy = Integer.parseInt(next + "", 16);
                    if(currentString.length() > 0) {
                        MComponent comp = new MTextComponent(currentString.toString()).withStyle(currentStyle);
                        out.add(comp);
                    }
                    currentString = new StringBuilder();

                    i += 1;
                    currentStyle = new MStyle().withColor(TextColor.fromRGBI(legacy));

                } else if(next == 'r') {

                    if(currentString.length() > 0) {
                        MComponent comp = new MTextComponent(currentString.toString()).withStyle(currentStyle);
                        out.add(comp);
                    }
                    currentString = new StringBuilder();
                    i += 1;

                    reset = true;

                    currentStyle = new MStyle();

                } else {
                    switch (next) {
                        case 'l': { currentStyle.withBold(true); i += 1; break; }
                        case 'o': { currentStyle.withItalic(true); i += 1; break; }
                        case 'n': { currentStyle.withUnderlined(true); i += 1; break; }
                        case 'm': { currentStyle.withStrikethrough(true); i += 1; break; }
                        case 'k': { currentStyle.withObfuscated(true); i += 1; break; }
                    }
                }

            } else if(c == '#' && i < content.length() - 7) {

                String hex = content.substring(i+1, i+7);
                MComponent comp = new MTextComponent(currentString.toString()).withStyle(currentStyle);
                out.add(comp);

                currentString = new StringBuilder();

                i += 6;
                currentStyle = new MStyle().withColor(new TextColor(hex));

            } else {
                currentString.append(c);
            }
        }

        out.add(new MTextComponent(currentString.toString()).withStyle(currentStyle));

        int i = 0;
        MComponent outComp = reset || out.get(0).getStyle().hasFormatting() ? new MTextComponent("") : out.get(i++);
        for(; i < out.size() ; i++) {
            outComp.addChild(out.get(i));
        }

        return outComp;
    }

    public static final ComponentSerializer SERIALIZER = new ComponentSerializer(true, true);
    public static final ComponentSerializer OBJECT_SERIALIZER = new ComponentSerializer(true, false);


    public static class ComponentSerializer implements Serializer<MComponent> {

        private final boolean tryParseStrings;
        private final boolean trySaveStrings;

        public ComponentSerializer(boolean tryParseStrings, boolean trySaveStrings) {
            this.tryParseStrings = tryParseStrings;
            this.trySaveStrings = trySaveStrings;
        }

        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> context, MComponent value) {

            if(trySaveStrings && !value.hasNonLegacyComponents()) {

                String s = value.toPlainText('&', getGameVersion() > 15 ? '#' : null);
                return SerializeResult.ofNullable(context.toString(s));
            }

            ComponentType type = value.type;
            SerializeResult<O> baseResult = type.serialize(this, context, value);
            if(!baseResult.isComplete()) return SerializeResult.failure(baseResult.getError());
            O out = baseResult.getOrThrow();

            if(value.style != null) {
                SerializeResult<O> styleResult = MStyle.SERIALIZER.serialize(context, value.style);
                if (!styleResult.isComplete()) return SerializeResult.failure(styleResult.getError());

                context.mergeMap(out, styleResult.getOrThrow());
            }

            if(value.hoverEvent != null) {
                SerializeResult<O> hoverResult = MHoverEvent.SERIALIZER.serialize(context, value.hoverEvent);
                if(!hoverResult.isComplete()) return SerializeResult.failure(hoverResult.getError());

                context.set("hoverEvent", hoverResult.getOrThrow(), out);
            }

            if(value.clickEvent != null) {
                SerializeResult<O> clickResult = MClickEvent.SERIALIZER.serialize(context, value.clickEvent);
                if(!clickResult.isComplete()) return SerializeResult.failure(clickResult.getError());

                context.set("clickEvent", clickResult.getOrThrow(), out);
            }

            if(value.insertion != null) {
                context.set("insertion", context.toString(value.insertion), out);
            }

            if(value.children.size() > 0) {
                List<O> extra = new ArrayList<>();
                for(MComponent child : value.children) {
                    SerializeResult<O> childResult = serialize(context, child);
                    if(!childResult.isComplete()) return SerializeResult.failure(childResult.getError());
                    extra.add(childResult.getOrThrow());
                }
                context.set("extra", context.toList(extra), out);
            }

            return SerializeResult.success(out);
        }

        @Override
        public <O> SerializeResult<MComponent> deserialize(SerializeContext<O> context, O value) {

            if(context.isString(value)) {
                if(tryParseStrings) {
                    String s = context.asString(value);
                    return SerializeResult.success(MComponent.parse(s));
                }
                return SerializeResult.success(new MTextComponent(context.asString(value)));
            }
            if(context.isBoolean(value)) {
                return SerializeResult.success(new MTextComponent(context.asBoolean(value).toString()));
            }
            if(context.isNumber(value)) {
                return SerializeResult.success(new MTextComponent(context.asNumber(value).toString()));
            }
            if(context.isList(value)) {
                List<MComponent> values = new ArrayList<>();
                for(O o : context.asList(value)) {

                    SerializeResult<MComponent> result = deserialize(context, o);
                    if(!result.isComplete()) {
                        return result;
                    }

                    values.add(result.getOrThrow());
                }

                MComponent out = values.get(0);
                for(int i = 1 ; i < values.size() ; i++) {
                    out.addChild(values.get(1));
                }

                return SerializeResult.success(out);
            }
            if(context.isMap(value)) {

                ComponentType type = null;
                for (String s : context.getOrderedKeys(value)) {
                    ComponentType ct = ComponentType.getById(s);
                    if (ct != null) {
                        type = ct;
                        break;
                    }
                }

                if(type == null) return SerializeResult.failure("Don't know to parse " + value + " with null type as a component!");

                SerializeResult<MComponent> result = type.deserialize(this, context, value);
                if(!result.isComplete()) return SerializeResult.failure(result.getError());

                MComponent out = result.getOrThrow();
                SerializeResult<MStyle> style = MStyle.SERIALIZER.deserialize(context, value);
                if(!style.isComplete()) return SerializeResult.failure(style.getError());

                out.withStyle(style.getOrThrow());

                if(context.get("hoverEvent", value) != null) {
                    SerializeResult<MHoverEvent> hover = MHoverEvent.SERIALIZER.deserialize(context, context.get("hoverEvent", value));
                    if(!hover.isComplete()) return SerializeResult.failure(hover.getError());
                    out.setHoverEvent(hover.getOrThrow());
                }

                if(context.get("clickEvent", value) != null) {
                    SerializeResult<MClickEvent> click = MClickEvent.SERIALIZER.deserialize(context, context.get("clickEvent", value));
                    if(!click.isComplete()) return SerializeResult.failure(click.getError());
                    out.setClickEvent(click.getOrThrow());
                }

                if(context.get("insertion", value) != null) {
                    O insertion = context.get("insertion", value);
                    String str = context.asString(insertion);
                    if(str == null) return SerializeResult.failure("Found invalid insertion " + insertion);
                    out.insertion = str;
                }

                // Extra
                if(context.get("extra", value) != null) {

                    O extra = context.get("extra", value);
                    if(context.isList(extra)) {

                        Collection<O> objects = context.asList(extra);
                        for(O o : objects) {
                            SerializeResult<MComponent> childResult = deserialize(context, o);
                            if(!childResult.isComplete()) return SerializeResult.failure(childResult.getError());
                            out.addChild(childResult.getOrThrow());
                        }
                    }
                }

                return SerializeResult.success(out);
            }

            return SerializeResult.failure("Don't know how to parse " + value + " as a component!");
        }
    }

    protected enum ComponentType {

        TEXT("text", parent -> ObjectSerializer.create(Serializer.STRING.entry("text", MComponent::getContent), MTextComponent::new)),
        TRANSLATE("translate", parent -> ObjectSerializer.create(
                Serializer.STRING.entry("translate", MComponent::getContent),
                parent.listOf().<MComponent>entry("with", mc -> ((MTranslateComponent) mc).getArgs()).optional(),
                MTranslateComponent::new
        ));

        final String id;
        final Function<Serializer<MComponent>, Serializer<MComponent>> serializer;

        ComponentType(String id, Function<Serializer<MComponent>, Serializer<MComponent>> serializer) {
            this.id = id;
            this.serializer = serializer;
        }
        <O> SerializeResult<MComponent> deserialize(Serializer<MComponent> parent, SerializeContext<O> context, O object) {
            return serializer.apply(parent).deserialize(context, object);
        }

        <O> SerializeResult<O> serialize(Serializer<MComponent> parent, SerializeContext<O> context, MComponent component) {
            return serializer.apply(parent).serialize(context, component);
        }

        static ComponentType getById(String id) {
            for(ComponentType type : values()) {
                if(type.id.equals(id)) return type;
            }
            return null;
        }
    }
}
