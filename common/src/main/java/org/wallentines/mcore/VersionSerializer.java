package org.wallentines.mcore;

import org.wallentines.mdcfg.Functions;
import org.wallentines.mdcfg.serializer.ContextSerializer;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

public interface VersionSerializer<T> extends ContextSerializer<T, GameVersion> {

    default Serializer<T> forCurrentVersion() {
        return forContext(GameVersion.CURRENT_VERSION.get());
    }

    static <T> VersionSerializer<T> fromStatic(Serializer<T> serializer) {

        return new VersionSerializer<>() {
            @Override
            public <O> SerializeResult<O> serialize(SerializeContext<O> context, T value, GameVersion version) {
                return serializer.serialize(context, value);
            }

            @Override
            public <O> SerializeResult<T> deserialize(SerializeContext<O> context, O value, GameVersion version) {
                return serializer.deserialize(context, value);
            }
        };
    }


}
