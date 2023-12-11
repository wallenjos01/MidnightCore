package org.wallentines.mcore.text;

import org.wallentines.mdcfg.serializer.InlineSerializer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

public class InlineContentSerializer<T extends Content> implements Serializer<Content> {

    private final Class<T> clazz;
    private final InlineSerializer<T> serializer;

    /**
     * Creates a content serializer for the given type
     * @param clazz The type of content to serialize
     * @param serializer The actual Serializer to use to serialize the content
     */
    public InlineContentSerializer(Class<T> clazz, InlineSerializer<T> serializer) {
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
