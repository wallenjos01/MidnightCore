package me.m1dnightninja.midnightcore.velocity.util;

import me.m1dnightninja.midnightcore.api.text.MComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class ConversionUtil {

    public static Component toKyoriComponent(MComponent comp) {

        return GsonComponentSerializer.gson().deserialize(MComponent.Serializer.toJsonString(comp));
    }

}
