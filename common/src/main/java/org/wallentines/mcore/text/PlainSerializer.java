package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

/**
 * A {@link ComponentSerializer} which serializes components into plain text, disregarding color or formatting information in the process
 */
public class PlainSerializer extends ComponentSerializer {

    /**
     * The global plain serializer instance
     */
    public static final PlainSerializer INSTANCE = new PlainSerializer();

    /**
     * Constructs a new PlainSerializer with all the default content serializer types
     */
    public PlainSerializer() {
        contentSerializers.register("text",      new ContentSerializer<>(Content.Text.class, Content.Text.PLAIN_SERIALIZER));
        contentSerializers.register("translate", new ContentSerializer<>(Content.Translate.class, Content.Translate.PLAIN_SERIALIZER));
        contentSerializers.register("keybind",   new ContentSerializer<>(Content.Keybind.class, Content.Keybind.PLAIN_SERIALIZER));
        contentSerializers.register("score",     new ContentSerializer<>(Content.Score.class, Content.Score.PLAIN_SERIALIZER));
        contentSerializers.register("selector",  new ContentSerializer<>(Content.Selector.class, Content.Selector.PLAIN_SERIALIZER));
        contentSerializers.register("nbt",       new ContentSerializer<>(Content.NBT.class, Content.NBT.PLAIN_SERIALIZER));
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value) {
        SerializeResult<O> res = serializeContent(context, value.content);
        if(!res.isComplete()) {
            return res;
        }
        O out = res.getOrThrow();
        if(!context.isString(out)) {
            return SerializeResult.failure("Unable to serialize " + value.content.type + " as a string!");
        }

        StringBuilder str = new StringBuilder(context.asString(out));
        for(Component comp : value.children) {
            SerializeResult<O> child = serialize(context, comp);
            if(!child.isComplete()) {
                return child;
            }
            str.append(context.asString(child.getOrThrow()));
        }

        return SerializeResult.success(context.toString(str.toString()));
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {

        if(!context.isString(value)) {
            return SerializeResult.failure("Cannot create plain text component out of non-string " + value);
        }

        return SerializeResult.success(Component.text(context.asString(value)));
    }
}
