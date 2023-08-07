package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link ComponentSerializer} which serializes components into maps in the modern component format
 */
public class ModernSerializer extends ComponentSerializer {

    /**
     * The global modern serializer instance
     */
    public static final ModernSerializer INSTANCE = new ModernSerializer();

    /**
     * Constructs a new ModernSerializer with all the default content serializer types
     */
    public ModernSerializer() {

        contentSerializers.register("text",      new ContentSerializer<>(Content.Text.class, Content.Text.SERIALIZER));
        contentSerializers.register("translate", new ContentSerializer<>(Content.Translate.class, Content.Translate.serializer(this)));
        contentSerializers.register("keybind",   new ContentSerializer<>(Content.Keybind.class, Content.Keybind.SERIALIZER));
        contentSerializers.register("score",     new ContentSerializer<>(Content.Score.class, Content.Score.SERIALIZER));
        contentSerializers.register("selector",  new ContentSerializer<>(Content.Selector.class, Content.Selector.SERIALIZER));
        contentSerializers.register("nbt",       new ContentSerializer<>(Content.NBT.class, Content.NBT.serializer(this)));
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value) {

        SerializeResult<O> content = serializeContent(context, value.content);
        if(!content.isComplete()) {
            return content;
        }

        O out = content.getOrThrow();

        String color = value.reset != null && value.reset ? "reset" : value.color == null ? null : TextColor.serialize(value.color);
        context.set("color", context.toString(color), out);

        context.set("bold", context.toBoolean(value.bold), out);
        context.set("italic", context.toBoolean(value.italic), out);
        context.set("underlined", context.toBoolean(value.underlined), out);
        context.set("strikethrough", context.toBoolean(value.strikethrough), out);
        context.set("obfuscated", context.toBoolean(value.obfuscated), out);

        context.set("font", context.toString(value.font == null ? null : value.font.toString()), out);
        context.set("insertion", context.toString(value.insertion), out);

        if(value.hoverEvent != null) {
            SerializeResult<O> hoverEvent = HoverEvent.SERIALIZER.serialize(context, value.hoverEvent);
            if(!hoverEvent.isComplete()) {
                return SerializeResult.failure("Unable to serialize hover event for component!" + hoverEvent.getError());
            }
            context.set("hoverEvent", hoverEvent.getOrThrow(), out);
        }

        if(value.clickEvent != null) {
            SerializeResult<O> clickEvent = ClickEvent.SERIALIZER.serialize(context, value.clickEvent);
            if(!clickEvent.isComplete()) {
                return SerializeResult.failure("Unable to serialize click event for component!" + clickEvent.getError());
            }
            context.set("clickEvent", clickEvent.getOrThrow(), out);
        }

        // Children
        if(value.children.size() > 0) {

            List<O> children = new ArrayList<>();
            for(Component cmp : value.children) {
                SerializeResult<O> result = serialize(context, cmp);
                if(!result.isComplete()) {
                    return SerializeResult.failure("Unable to serialize children for component!" + result.getError());
                }
                children.add(result.getOrThrow());
            }

            context.set("extra", context.toList(children), out);

        }

        return SerializeResult.success(out);
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {

        if(context.isString(value)) {
            return SerializeResult.success(Component.text(context.asString(value)));
        }
        if(context.isBoolean(value)) {
            return SerializeResult.success(Component.text(String.valueOf(context.asBoolean(value))));
        }
        if(context.isNumber(value)) {
            return SerializeResult.success(Component.text(String.valueOf(context.asNumber(value))));
        }
        if(context.isList(value)) {
            return deserializeList(context, context.asList(value));
        }
        if(context.isMap(value)) {
            return deserializeMap(context, value);
        }

        return SerializeResult.failure("Unable to turn " + value + " into a component!");
    }


    private <O> SerializeResult<Component> deserializeList(SerializeContext<O> context, Collection<O> value) {
        List<Component> values = new ArrayList<>();

        if(value.isEmpty()) {
            return SerializeResult.success(Component.text(""));
        }

        for(O o : value) {

            SerializeResult<Component> res = deserialize(context, o);
            if(!res.isComplete()) return res;

            values.add(res.getOrThrow());
        }

        return SerializeResult.success(values.get(0).addChildren(values.subList(1, values.size())));
    }

    private <O> SerializeResult<Component> deserializeMap(SerializeContext<O> context, O value) {

        Component out = null;
        for(String s : context.getOrderedKeys(value)) {

            if(contentSerializers.contains(s)) {

                SerializeResult<? extends Content> con = contentSerializers.get(s).deserialize(context, value);
                if(!con.isComplete()) {
                    return SerializeResult.failure("Unable to deserialize component contents! " + con.getError());
                }

                out = new Component(con.getOrThrow());
                break;
            }
        }

        if(out == null) {
            return SerializeResult.failure("Unable to deserialize component contents! Could not find valid content type!");
        }

        String clr = context.asString(context.get("color", value));
        if(clr.equals("reset")) {
            out = out.withReset(true);
        } else {
            out = out.withColor(TextColor.parse(clr));
        }

        out = Serializer.BOOLEAN.deserialize(context, context.get("bold", value)).get().map(out::withBold).orElse(out);
        out = Serializer.BOOLEAN.deserialize(context, context.get("italic", value)).get().map(out::withItalic).orElse(out);
        out = Serializer.BOOLEAN.deserialize(context, context.get("underlined", value)).get().map(out::withUnderlined).orElse(out);
        out = Serializer.BOOLEAN.deserialize(context, context.get("strikethrough", value)).get().map(out::withStrikethrough).orElse(out);
        out = Serializer.BOOLEAN.deserialize(context, context.get("obfuscated", value)).get().map(out::withObfuscated).orElse(out);
        out = Identifier.serializer("minecraft").deserialize(context, context.get("font", value)).get().map(out::withFont).orElse(out);
        out = Serializer.STRING.deserialize(context, context.get("insertion", value)).get().map(out::withInsertion).orElse(out);
        out = HoverEvent.SERIALIZER.deserialize(context, context.get("hoverEvent", value)).get().map(out::withHoverEvent).orElse(out);
        out = ClickEvent.SERIALIZER.deserialize(context, context.get("clickEvent", value)).get().map(out::withClickEvent).orElse(out);

        O extra = context.get("extra", value);
        if(context.isList(extra)) {

            Collection<O> values = context.asList(extra);
            for(O o : values) {
                SerializeResult<Component> child = deserialize(context, o);
                if(!child.isComplete()) {
                    return SerializeResult.failure("Unable to deserialize component extra! " + child.getError());
                }
                out.addChild(child.getOrThrow());
            }
        }

        return SerializeResult.success(out);

    }
}
