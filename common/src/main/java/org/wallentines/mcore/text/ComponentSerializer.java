package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.StringRegistry;

/**
 * A special {@link org.wallentines.mdcfg.serializer.Serializer Serializer} for a component, its contents,
 * and its children.
 */
public abstract class ComponentSerializer implements Serializer<Component> {

    /**
     * A registry for content serializers corresponding to the component serializer.
     */
    public final StringRegistry<ContentSerializer<?>> contentSerializers = new StringRegistry<>();

    /**
     * Attempts to serialize a component {@link org.wallentines.mcore.text.Content Content} using the
     * {@link #contentSerializers} Registry
     * @param context The {@link org.wallentines.mdcfg.serializer.SerializeContext SerializeContext} by which to serialize
     * @param content The content to serialize
     * @return The SerializeResult containing content serialized as an object of type O
     * @param <O> The type of data to serialize into
     */
    public <O> SerializeResult<O> serializeContent(SerializeContext<O> context, Content content) {
        ContentSerializer<?> ser = contentSerializers.get(content.type);
        if(ser == null) {
            return SerializeResult.failure("Unable to serialize component! Unable to find serializer for content type " + content.type + "!");
        }

        return ser.serialize(context, content);
    }

}
