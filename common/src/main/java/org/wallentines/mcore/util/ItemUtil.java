package org.wallentines.mcore.util;

import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.util.*;

public class ItemUtil {

    /**
     * Splits a UUID into four 32-bit integers
     * @param uuid The UUID to split
     * @return An integer array of length 4
     */
    public static int[] splitUUID(UUID uuid) {
        long u1 = uuid.getMostSignificantBits();
        long u2 = uuid.getLeastSignificantBits();
        return new int[] { (int) (u1 >> 32), (int) u1, (int) (u2 >> 32), (int) u2 };
    }

    /**
     * Creates a UUID from four 32-bit integers
     * @param ints The data to create a UUID from
     * @return A UUID
     */
    public static UUID joinUUID(int[] ints) {

        if(ints.length != 4) {
            MidnightCoreAPI.LOGGER.warn("Attempt to form UUID from array of length " + ints.length + "! Expected length 4!");
            return null;
        }

        return new UUID(
                (long) ints[0] << 32 | (long) ints[1] & 0xFFFFFFFFL,
                (long) ints[2] << 32 | (long) ints[3] & 0xFFFFFFFFL
        );
    }

    /**
     * Set italic to false on the component if unset, so components appear as intended on ItemStacks
     * @param component The component to modify
     * @return A modified component
     */
    public static Component applyItemNameBaseStyle(Component component) {
        Component out = component;
        if(out.italic == null) {
            out = out.withItalic(false);
        }
        return out;
    }

    /**
     * Set italic to false and color to white on the component if unset, so components appear as intended on ItemStacks
     * @param component The component to modify
     * @return A modified component
     */
    public static Component applyItemLoreBaseStyle(Component component) {
        Component out = component;
        if(out.italic == null) {
            out = out.withItalic(false);
        }
        if(out.color == null) {
            out = out.withColor(TextColor.WHITE);
        }
        return out;
    }

    /**
     * Converts an object to an NBT string using the given context
     * @param context The context by which to convert
     * @param object The config object to convert
     * @return A string representation of an NBT tag
     */
    public static <T> String toNBTString(SerializeContext<T> context, T object) {

        // Strings
        if(context.isString(object)) {
            return "\"" + context.asString(object) + "\"";
        }
        // Numbers
        if(context.isNumber(object)) {
            Number number = context.asNumber(object);
            if(number instanceof Byte) { return number.byteValue() + "b"; }
            if(number instanceof Short) { return number.shortValue() + "s"; }
            if(number instanceof Integer) { return number.intValue() + ""; }
            if(number instanceof Long) { return number.longValue() + "l"; }
            if(number instanceof Float) { return number.floatValue() + "f"; }
            if(number instanceof Double) { return number.doubleValue() + "d"; }

            if(ConfigPrimitive.isInteger(number)) {
                return number.longValue() + "l";
            } else {
                return number.doubleValue() + "d";
            }
        }
        // Booleans
        if(context.isBoolean(object)) {
            return context.asBoolean(object) ? "1b" : "0b";
        }
        // Lists
        if(context.isList(object)) {

            boolean maybeByte = true;
            boolean maybeInt = true;
            boolean maybeLong = true;

            Collection<T> values = context.asList(object);
            Iterator<T> it = values.iterator();
            while((maybeByte || maybeLong || maybeInt) && it.hasNext()) {
                T value = it.next();
                if(!context.isNumber(value)) {
                    maybeByte = false;
                    maybeInt = false;
                    maybeLong = false;
                    break;
                }

                Number num = context.asNumber(value);
                if(!(num instanceof Byte)) maybeByte = false;
                if(!(num instanceof Integer)) maybeInt = false;
                if(!(num instanceof Long)) maybeLong = false;
            }

            StringBuilder out = new StringBuilder("[");
            if(maybeByte) {
                out.append("B;");
                for(T t : values) out.append(context.asNumber(t).byteValue()).append("b");
            } else if(maybeInt) {
                out.append("I;");
                for(T t : values) out.append(context.asNumber(t).byteValue());
            } else if(maybeLong) {
                out.append("L;");
                for(T t : values) out.append(context.asNumber(t).byteValue()).append("l");
            } else {
                for(T t : values) out.append(toNBTString(context, t));
            }
            out.append("]");
            return out.toString();
        }
        // Compounds
        if(context.isMap(object)) {
            Map<String, T> values = context.asMap(object);
            StringBuilder out = new StringBuilder("{");
            for(Map.Entry<String, T> ent : values.entrySet()) {
                out.append("\"").append(ent.getKey()).append("\":");
                out.append(toNBTString(context, ent.getValue()));
            }
            out.append("}");
            return out.toString();
        }

        throw new IllegalArgumentException("Don't know how to turn " + object + " into a NBT String!");
    }
}
