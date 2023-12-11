package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.RegistryBase;

/**
 * A {@link Serializer} which serializes components into plain text, disregarding color or formatting information in the process
 */
public class PlainSerializer implements Serializer<Component> {

    /**
     * The global plain serializer instance
     */
    public static final PlainSerializer INSTANCE = new PlainSerializer(LegacySerializer.CONTENT_SERIALIZERS);

    private final RegistryBase<String, InlineContentSerializer<?>> serializers;

    /**
     * Constructs a new PlainSerializer with all the default content serializer types
     * @param serializers The content serializers to use
     */
    public PlainSerializer(RegistryBase<String, InlineContentSerializer<?>> serializers) {
        this.serializers = serializers;
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

    public <O> SerializeResult<O> serializeContent(SerializeContext<O> context, Content value) {

        InlineContentSerializer<?> ser = serializers.get(value.type);
        if(ser == null) {
            return SerializeResult.failure("Serializer for component contents with type " + value.type + " not found!");
        }

        return ser.serialize(context, value);
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {

        if(!context.isString(value)) {
            return SerializeResult.failure("Cannot create plain text component out of non-string " + value);
        }

        return SerializeResult.success(Component.text(context.asString(value)));
    }
}
