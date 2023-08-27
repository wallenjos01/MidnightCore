package org.wallentines.mcore.adapter.v1_20_R1;

import net.minecraft.nbt.*;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;

import java.util.stream.Collectors;

public class NBTConverter {


    public static NBTBase toNBT(ConfigObject config) {

        if(config.isBoolean()) {
            return NBTTagByte.a(config.asBoolean());
        }
        if(config.isString()) {
            return NBTTagString.a(config.asString());
        }
        if(config.isNumber()) {
            Number num = config.asNumber();
            if(num instanceof Byte) return NBTTagByte.a(num.byteValue());
            if(num instanceof Short) return NBTTagShort.a(num.shortValue());
            if(num instanceof Integer) return NBTTagInt.a(num.intValue());
            if(num instanceof Long) return NBTTagLong.a(num.longValue());
            if(num instanceof Float) return NBTTagFloat.a(num.floatValue());
            if(num instanceof Double) return NBTTagDouble.a(num.doubleValue());
            if(ConfigPrimitive.isInteger(num)) {
                return NBTTagLong.a(num.longValue());
            } else {
                return NBTTagDouble.a(num.doubleValue());
            }
        }
        if(config.isList()) {
            ConfigList list = config.asList();
            boolean byteArray = list.stream().allMatch(it -> it.isNumber() && it.asNumber() instanceof Byte);
            boolean intArray = list.stream().allMatch(it -> it.isNumber() && it.asNumber() instanceof Integer);
            boolean longArray = list.stream().allMatch(it -> it.isNumber() && it.asNumber() instanceof Long);

            if(byteArray) {
                return new NBTTagByteArray(list.stream().map(it -> it.asNumber().byteValue()).collect(Collectors.toList()));
            }
            if(intArray) {
                return new NBTTagIntArray(list.stream().map(it -> it.asNumber().intValue()).collect(Collectors.toList()));
            }
            if(longArray) {
                return new NBTTagLongArray(list.stream().map(it -> it.asNumber().longValue()).collect(Collectors.toList()));
            }

            NBTTagList out = new NBTTagList();
            list.stream().map(NBTConverter::toNBT).forEach(out::add);

            return out;
        }
        if(config.isSection()) {
            NBTTagCompound out = new NBTTagCompound();
            for(String key : config.asSection().getKeys()) {
                out.a(key, toNBT(config.asSection().getOrThrow(key)));
            }
            return out;
        }

        throw new IllegalArgumentException("Don't know how to convert " + config + " into an NBT tag!");
    }

    public static ConfigObject fromNBT(NBTBase nbt) {

        if(nbt instanceof NBTTagString) {
            return new ConfigPrimitive(nbt.toString());
        }
        if(nbt instanceof NBTNumber) {
            return new ConfigPrimitive(((NBTNumber) nbt).l());
        }
        if(nbt instanceof NBTList<?>) {
            ConfigList out = new ConfigList();
            for(NBTBase base : ((NBTList<?>) nbt)) {
                out.add(fromNBT(base));
            }
            return out;
        }
        if(nbt instanceof NBTTagCompound) {
            ConfigSection out = new ConfigSection();
            for(String key : ((NBTTagCompound) nbt).e()) {
                out.set(key, fromNBT(((NBTTagCompound) nbt).c(key)));
            }
            return out;
        }
        return null;
    }

}
