package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.VersionSerializer;
import org.wallentines.mdcfg.serializer.ContextSerializer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

/**
 * A Special type of serializer for Content types. To be used within {@link org.wallentines.mcore.text.ComponentSerializer ComponentSerializers}
 * @param <T>
 */
public class ContentSerializer<T extends Content> implements ContextSerializer<Content, ContentSerializer.Context> {

    private final Class<T> clazz;
    private final ContextSerializer<T, ContentSerializer.Context> serializer;

    /**
     * Creates a content serializer for the given type
     * @param clazz The type of content to serialize
     * @param serializer The actual Serializer to use to serialize the content
     */
    public ContentSerializer(Class<T> clazz, ContextSerializer<T, ContentSerializer.Context> serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Content value, Context ctx) {

        if(!clazz.isAssignableFrom(value.getClass())) {
            return SerializeResult.failure("Unable to serialize " + value + "! Expected class type " + clazz.getCanonicalName() + "!");
        }

        return serializer.serialize(context, clazz.cast(value), ctx);
    }

    @Override
    public <O> SerializeResult<Content> deserialize(SerializeContext<O> context, O value, Context ctx) {
        return serializer.deserialize(context, value, ctx).flatMap(con -> con);
    }

    public static class Context {

        public final GameVersion version;
        public final VersionSerializer<Component> serializer;

        public Context(GameVersion version, VersionSerializer<Component> serializer) {
            this.version = version;
            this.serializer = serializer;
        }
    }


    public static final ContextSerializer<Component, Context> COMPONENT = new ContextSerializer<>() {
        @Override
        public <O> SerializeResult<O> serialize(SerializeContext<O> serializeContext, Component component, Context context) {
            return context.serializer.serialize(serializeContext, component, context.version);
        }

        @Override
        public <O> SerializeResult<Component> deserialize(SerializeContext<O> serializeContext, O o, Context context) {
            return context.serializer.deserialize(serializeContext, o, context.version);
        }
    };

}
