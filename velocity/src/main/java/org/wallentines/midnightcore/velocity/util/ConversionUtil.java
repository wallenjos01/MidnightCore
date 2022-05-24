package org.wallentines.midnightcore.velocity.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.registry.Identifier;

public class ConversionUtil {

    public static Component toComponent(MComponent component) {

        return GsonComponentSerializer.gson().deserializeFromTree(MComponent.SERIALIZER.serialize(component).toJson());
    }

    public static Key toKey(Identifier id) {
        return Key.key(id.getNamespace(), id.getPath());
    }

}
