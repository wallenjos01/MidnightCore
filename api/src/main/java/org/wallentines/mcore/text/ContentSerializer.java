package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

/**
 * A Special type of serializer for Content types. To be used within {@link org.wallentines.mcore.text.ComponentSerializer ComponentSerializers}
 * @param <T>
 */
public class ContentSerializer<T extends Content> implements Serializer<Content> {

    private final Class<T> clazz;
    private final Serializer<T> serializer;

    /**
     * Creates a content serializer for the given type
     * @param clazz The type of content to serialize
     * @param serializer The actual Serializer to use to serialize the content
     */
    public ContentSerializer(Class<T> clazz, Serializer<T> serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Content value) {

        if(!clazz.isAssignableFrom(value.getClass())) {
            return SerializeResult.failure("Unable to serialize " + value + "! Expected class type " + clazz.getCanonicalName() + "!");
        }

        return serializer.serialize(context, clazz.cast(value));
    }

    @Override
    public <O> SerializeResult<Content> deserialize(SerializeContext<O> context, O value) {
        return serializer.deserialize(context, value).flatMap(con -> con);
    }
}
