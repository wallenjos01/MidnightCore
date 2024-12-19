package org.wallentines.mcore.text;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.mdcfg.serializer.Serializer;

/**
 * A {@link Serializer} which serializes components into Strings optimized for config files.
 */
public class ConfigSerializer implements Serializer<Component> {

    /**
     * The global instance of a ConfigSerializer
     */
    public static final ConfigSerializer INSTANCE = new ConfigSerializer(true);

    /**
     * The global instance of a ConfigSerializer
     */
    public static final ConfigSerializer NO_JSON = new ConfigSerializer(false);

    private final boolean tryParseJSON;

    public ConfigSerializer(boolean tryParseJSON) {
        this.tryParseJSON = tryParseJSON;
    }

    @Override
    public <O> SerializeResult<O> serialize(SerializeContext<O> context, Component value) {

        if(value.hasNonLegacyComponents()) {
            return ModernSerializer.INSTANCE.serialize(GameVersion.context(), value).flatMap(o -> context.toString(JSONCodec.minified().encodeToString(GameVersion.context(), o)));
        }

        return LegacySerializer.CONFIG_INSTANCE.serialize(context, value);
    }

    @Override
    public <O> SerializeResult<Component> deserialize(SerializeContext<O> context, O value) {

        if(context.isString(value)) {

            if(tryParseJSON) {

                String s = context.asString(value).getOrNull();

                if(s != null) {
                String stripped = s.stripLeading();
                if (stripped.isEmpty()) return SerializeResult.success(Component.text(s));
                if (stripped.charAt(0) == '{') {
                    try {
                        O sec = JSONCodec.minified().decode(context, stripped);
                        return ModernSerializer.INSTANCE.deserialize(context, sec);
                    } catch (DecodeException ex) {
                        // Ignore
                    }
                }
                }
            }

            return LegacySerializer.CONFIG_INSTANCE.deserialize(context, value);

        } else {

            return ModernSerializer.INSTANCE.deserialize(context, value);
        }
    }
}
