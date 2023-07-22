package org.wallentines.mcore.util;

import net.minecraft.nbt.*;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A {@link org.wallentines.mdcfg.serializer.SerializeContext SerializeContext} for serializing to or deserialize from
 * Minecraft NBT tags
 */
public class NBTContext implements SerializeContext<Tag> {

    /**
     * The global NBT Context instance which will attempt to serialize numeric arrays.
     */
    public static final NBTContext INSTANCE = new NBTContext(true);

    private final boolean tryNumericArrays;

    /**
     * Constructs a new NBTContext which will not attempt to serialize numeric arrays
     */
    public NBTContext() {
        this(false);
    }

    /**
     * Constructs a new NBTContext which optionally attempts to serialize numeric arrays
     * @param tryNumericArrays Whether this context should attempt to serialize lists as array tags such as IntArrayTag,
     *                         ByteArrayTag, or LongArrayTag if all the items in the list are of the same type. This is
     *                         necessary in certain contexts, such as when UUIDs are present in NBT tags.
     */
    public NBTContext(boolean tryNumericArrays) {
        this.tryNumericArrays = tryNumericArrays;
    }

    @Override
    public String asString(Tag object) {
        return isString(object) ? object.getAsString() : null;
    }

    @Override
    public Number asNumber(Tag object) {
        return isNumber(object) ? ((NumericTag) object).getAsNumber() : null;
    }

    @Override
    public Boolean asBoolean(Tag object) {
        return isBoolean(object) ? ((ByteTag) object).getAsByte() != 0 : null;
    }

    @Override
    public Collection<Tag> asList(Tag object) {

        return isList(object) ? new ArrayList<>((CollectionTag<?>) object) : null;
    }

    @Override
    public Map<String, Tag> asMap(Tag object) {
        return isMap(object) ? ((CompoundTag) object).getAllKeys().stream()
                .map(key -> new Tuples.T2<>(key, ((CompoundTag) object).get(key)))
                .collect(Collectors.toMap(t2 -> t2.p1, t2 -> t2.p2)) : null;
    }

    @Override
    public Map<String, Tag> asOrderedMap(Tag object) {
        return asMap(object);
    }

    @Override
    public boolean isString(Tag object) {
        if(object == null) return false;
        return object.getType() == StringTag.TYPE;
    }

    @Override
    public boolean isNumber(Tag object) {
        if(object == null) return false;
        return object.getType() == IntTag.TYPE ||
                object.getType() == LongTag.TYPE ||
                object.getType() == ShortTag.TYPE ||
                object.getType() == ByteTag.TYPE ||
                object.getType() == FloatTag.TYPE ||
                object.getType() == DoubleTag.TYPE;
    }

    @Override
    public boolean isBoolean(Tag object) {
        if(object == null) return false;
        return object.getType() == ByteTag.TYPE;
    }

    @Override
    public boolean isList(Tag object) {
        if(object == null) return false;
        return object.getType() == ListTag.TYPE ||
                object.getType() == IntArrayTag.TYPE ||
                object.getType() == LongArrayTag.TYPE ||
                object.getType() == ByteArrayTag.TYPE;
    }

    @Override
    public boolean isMap(Tag object) {
        if(object == null) return false;
        return object.getType() == CompoundTag.TYPE;
    }

    @Override
    public Collection<String> getOrderedKeys(Tag object) {
        if(!isMap(object)) return null;
        return ((CompoundTag) object).getAllKeys();
    }

    @Override
    public Tag get(String key, Tag object) {
        if(!isMap(object)) return null;
        return ((CompoundTag) object).get(key);
    }

    @Override
    public Tag toString(String object) {
        if(object == null) return null;
        return StringTag.valueOf(object);
    }

    @Override
    public Tag toNumber(Number object) {
        if(object == null) return null;
        if(object instanceof Integer) {
            return IntTag.valueOf(object.intValue());
        }
        if(object instanceof Double) {
            return DoubleTag.valueOf(object.doubleValue());
        }
        if(object instanceof Float) {
            return FloatTag.valueOf(object.floatValue());
        }
        if(object instanceof Long) {
            return LongTag.valueOf(object.longValue());
        }
        if(object instanceof Short) {
            return ShortTag.valueOf(object.shortValue());
        }
        if(object instanceof Byte) {
            return ByteTag.valueOf(object.byteValue());
        }
        return null;
    }

    @Override
    public Tag toBoolean(Boolean object) {
        if(object == null) return null;
        return ByteTag.valueOf(object);
    }

    @Override
    public Tag toList(Collection<Tag> list) {
        if(list == null) return null;
        if (list.size() == 0) {
            return new ListTag();
        }

        if(tryNumericArrays) {

            int byteCount = 0;
            byte[] bytes = new byte[list.size()];
            int intCount = 0;
            int[] ints = new int[list.size()];
            int longCount = 0;
            long[] longs = new long[list.size()];

            for(Tag t : list) {
                if(t.getType() == ByteTag.TYPE) {
                    bytes[byteCount++] = ((ByteTag) t).getAsByte();
                }
                else if(t.getType() == IntTag.TYPE) {
                    ints[intCount++] = ((IntTag) t).getAsInt();
                }
                else if(t.getType() == LongTag.TYPE) {
                    longs[longCount++] = ((LongTag) t).getAsLong();
                }
            }

            if(intCount == list.size()) {
                return new IntArrayTag(ints);
            }
            if(byteCount == list.size()) {
                return new ByteArrayTag(bytes);
            }
            if(longCount == list.size()) {
                return new LongArrayTag(longs);
            }

        }

        ListTag out = new ListTag();
        out.addAll(list);
        return out;
    }

    @Override
    public Tag toMap(Map<String, Tag> map) {
        if(map == null) return null;
        CompoundTag tag = new CompoundTag();
        map.forEach(tag::put);
        return tag;
    }

    @Override
    public Tag mergeList(Collection<Tag> list, Tag object) {
        if(!isList(object)) return null;
        ListTag arr = (ListTag) object;
        if(list != null) arr.addAll(list);
        return arr;
    }

    @Override
    public Tag mergeMap(Tag value, Tag other) {
        if(!isMap(value) || !isMap(other)) return null;

        CompoundTag base = (CompoundTag) value;
        CompoundTag fill = (CompoundTag) other;

        for(String key : fill.getAllKeys()) {
            if(!base.contains(key)) {
                base.put(key, fill.get(key));
            }
        }

        return base;
    }

    @Override
    public Tag mergeMapOverwrite(Tag value, Tag other) {
        if(!isMap(value) || !isMap(other)) return null;

        CompoundTag base = (CompoundTag) value;
        CompoundTag fill = (CompoundTag) other;

        base.merge(fill);
        return base;
    }

    @Override
    public Tag set(String key, Tag value, Tag object) {
        if(!isMap(object)) return null;
        ((CompoundTag) object).put(key, value);
        return object;
    }
}
