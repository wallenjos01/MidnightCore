package org.wallentines.midnightcore.velocity.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.registry.Identifier;

public class ConversionUtil {

    public static Component toComponent(MComponent component) {

        if(component == null) return null;
        return GsonComponentSerializer.gson().deserialize(component.toJSONString());
    }

    public static MComponent toMComponent(Component component) {

        if(component == null) return null;
        return MComponent.parseJSON(GsonComponentSerializer.gson().serialize(component));
    }


    public static Key toKey(Identifier id) {
        return Key.key(id.getNamespace(), id.getPath());
    }

}
