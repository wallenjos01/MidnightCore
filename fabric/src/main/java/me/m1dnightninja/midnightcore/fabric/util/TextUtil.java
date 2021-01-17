package me.m1dnightninja.midnightcore.fabric.util;

import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

public final class TextUtil {

    public static MutableComponent parse(String s) {

        try {
            return Component.Serializer.fromJson(s);
        } catch (JsonParseException ex) {
            return new TextComponent(s);
        }
    }

}
