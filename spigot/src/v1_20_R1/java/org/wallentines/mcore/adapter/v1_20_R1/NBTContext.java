package org.wallentines.mcore.adapter.v1_20_R1;

import net.minecraft.nbt.*;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class NBTContext implements SerializeContext<NBTBase> {

    public NBTContext() { }
    @Override public String asString(NBTBase object) { return isString(object) ? object.m_() : null; }
    @Override public Number asNumber(NBTBase object) { return isNumber(object) ? ((NBTNumber) object).l() : null; }
    @Override public Boolean asBoolean(NBTBase object) { return isBoolean(object) ? ((NBTTagByte) object).j() != 0 : null; }
    @Override public Collection<NBTBase> asList(NBTBase object) { return isList(object) ? new ArrayList<>((NBTList<?>) object) : null; }

    @Override
    public Map<String, NBTBase> asMap(NBTBase object) {
        return isMap(object) ? ((NBTTagCompound) object).e().stream()
                .map(key -> new Tuples.T2<>(key, ((NBTTagCompound) object).c(key)))
                .collect(Collectors.toMap(t2 -> t2.p1, t2 -> t2.p2)) : null;
    }

    @Override public Map<String, NBTBase> asOrderedMap(NBTBase object) { return asMap(object); }
    @Override public boolean isString(NBTBase object) { return object != null && object.c() == NBTTagString.a; }

    @Override
    public boolean isNumber(NBTBase object) {
        if(object == null) return false;
        return object instanceof NBTNumber;
    }

    @Override
    public boolean isBoolean(NBTBase object) {
        return object instanceof NBTTagByte;
    }

    @Override
    public boolean isList(NBTBase object) {
        return object instanceof NBTList<?>;
    }

    @Override
    public boolean isMap(NBTBase object) {
        return object instanceof NBTTagCompound;
    }

    @Override
    public Collection<String> getOrderedKeys(NBTBase object) {
        if(!isMap(object)) return null;
        return ((NBTTagCompound) object).e();
    }

    @Override
    public NBTBase get(String key, NBTBase object) {
        if(!isMap(object)) return null;
        return ((NBTTagCompound) object).c(key);
    }

    @Override
    public NBTBase toString(String object) {
        if(object == null) return null;
        return NBTTagString.a(object);
    }

    @Override
    public NBTBase toNumber(Number object) {
        if(object == null) return null;
        if(object instanceof Integer) {
            return NBTTagInt.a(object.intValue());
        }
        if(object instanceof Double) {
            return NBTTagDouble.a(object.doubleValue());
        }
        if(object instanceof Float) {
            return NBTTagFloat.a(object.floatValue());
        }
        if(object instanceof Long) {
            return NBTTagLong.a(object.longValue());
        }
        if(object instanceof Short) {
            return NBTTagShort.a(object.shortValue());
        }
        if(object instanceof Byte) {
            return NBTTagByte.a(object.byteValue());
        }
        return ConfigPrimitive.isInteger(object) ? NBTTagLong.a(object.longValue()) : NBTTagDouble.a(object.doubleValue());
    }

    @Override
    public NBTBase toBoolean(Boolean object) {
        if(object == null) return null;
        return NBTTagByte.a(object);
    }

    @Override
    public NBTBase toList(Collection<NBTBase> list) {
        if(list == null) return null;
        if (list.isEmpty()) {
            return new NBTTagList();
        }

        boolean allBytes = list.stream().allMatch(nbt -> nbt instanceof NBTTagByte);
        boolean allInts = list.stream().allMatch(nbt -> nbt instanceof NBTTagInt);
        boolean allLongs = list.stream().allMatch(nbt -> nbt instanceof NBTTagLong);

        if(allBytes) {
            return new NBTTagByteArray(list.stream().map(nbt -> ((NBTNumber) nbt).l().byteValue()).toList());
        }
        if(allInts) {
            return new NBTTagIntArray(list.stream().map(nbt -> ((NBTNumber) nbt).l().intValue()).toList());
        }
        if(allLongs) {
            return new NBTTagLongArray(list.stream().map(nbt -> ((NBTNumber) nbt).l().longValue()).toList());
        }

        NBTTagList out = new NBTTagList();
        out.addAll(list);
        return out;
    }

    @Override
    public NBTBase toMap(Map<String, NBTBase> map) {
        if(map == null) return null;
        NBTTagCompound tag = new NBTTagCompound();
        map.forEach(tag::a);
        return tag;
    }

    @Override
    public NBTBase mergeList(Collection<NBTBase> list, NBTBase object) {
        if(!(object instanceof NBTTagList arr)) return null;
        if(list != null) arr.addAll(list);
        return arr;
    }

    @Override
    public NBTBase mergeMap(NBTBase value, NBTBase other) {
        if(!isMap(value) || !isMap(other)) return null;

        NBTTagCompound base = (NBTTagCompound) value;
        NBTTagCompound fill = (NBTTagCompound) other;

        for(String key : fill.e()) {
            if(!base.b(key)) {
                base.a(key, fill.c(key));
            }
        }

        return base;
    }

    @Override
    public NBTBase mergeMapOverwrite(NBTBase value, NBTBase other) {
        return mergeMap(other, value);
    }

    @Override
    public NBTBase set(String key, NBTBase value, NBTBase object) {
        if(!isMap(object)) return null;
        ((NBTTagCompound) object).a(key, value);
        return object;
    }
}
