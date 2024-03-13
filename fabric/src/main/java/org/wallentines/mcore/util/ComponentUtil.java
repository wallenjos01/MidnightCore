package org.wallentines.mcore.util;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigObject;

public class ComponentUtil {

    public static <T> ConfigObject encode(DataComponentType<T> type, T value) {
        return type.codecOrThrow().encodeStart(ConfigOps.INSTANCE, value).getOrThrow(false, MidnightCoreAPI.LOGGER::error);
    }

    public static <T> ConfigObject encodeTyped(TypedDataComponent<T> type) {
        return type.encodeValue(ConfigOps.INSTANCE).getOrThrow(false, MidnightCoreAPI.LOGGER::error);
    }

    public static <T> T decode(DataComponentType<T> type, ConfigObject value) {
        return type.codecOrThrow().decode(ConfigOps.INSTANCE, value).getOrThrow(false, MidnightCoreAPI.LOGGER::error).getFirst();
    }

    public static <T> TypedDataComponent<T> decodeTyped(DataComponentType<T> type, ConfigObject value) {
        return new TypedDataComponent<>(type, decode(type, value));
    }

}
