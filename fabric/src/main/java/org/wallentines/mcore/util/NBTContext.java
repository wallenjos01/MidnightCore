package org.wallentines.mcore.util;

import net.minecraft.nbt.*;
import org.wallentines.mdcfg.ByteBufferInputStream;
import org.wallentines.mdcfg.Tuples;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
    public SerializeResult<String> asString(Tag object) {
        return isString(object) ? SerializeResult.success(object.getAsString()) : SerializeResult.failure("Not a string");
    }

    @Override
    public SerializeResult<Number> asNumber(Tag object) {
        return isNumber(object) ? SerializeResult.success(((NumericTag) object).getAsNumber()) : SerializeResult.failure("Not a number");
    }

    @Override
    public SerializeResult<Boolean> asBoolean(Tag object) {
        return isBoolean(object) ? SerializeResult.success(((ByteTag) object).getAsByte() != 0) : SerializeResult.failure("Not a boolean");
    }

    @Override
    public SerializeResult<ByteBuffer> asBlob(Tag object) {
        if(!isBlob(object)) return SerializeResult.failure("Not a blob");
        return SerializeResult.success(ByteBuffer.wrap(((ByteArrayTag) object).getAsByteArray()));
    }

    @Override
    public SerializeResult<Collection<Tag>> asList(Tag object) {

        return isList(object) ? SerializeResult.success(new ArrayList<>((CollectionTag<?>) object)) : SerializeResult.failure("Not a list");
    }

    @Override
    public SerializeResult<Map<String, Tag>> asMap(Tag object) {
        return isMap(object) ? SerializeResult.success(((CompoundTag) object).getAllKeys().stream()
                .map(key -> new Tuples.T2<>(key, ((CompoundTag) object).get(key)))
                .collect(Collectors.toMap(t2 -> t2.p1, t2 -> t2.p2))) : SerializeResult.failure("Not a map");
    }

    @Override
    public SerializeResult<Map<String, Tag>> asOrderedMap(Tag object) {
        return asMap(object);
    }

    @Override
    public Type getType(Tag object) {

        return switch (object.getId()) {
            case Tag.TAG_END -> Type.NULL;
            case Tag.TAG_BYTE, Tag.TAG_SHORT, Tag.TAG_INT, Tag.TAG_LONG, Tag.TAG_FLOAT, Tag.TAG_DOUBLE -> Type.NUMBER;
            case Tag.TAG_BYTE_ARRAY -> Type.BLOB;
            case Tag.TAG_STRING -> Type.STRING;
            case Tag.TAG_LIST, Tag.TAG_INT_ARRAY, Tag.TAG_LONG_ARRAY -> Type.LIST;
            case Tag.TAG_COMPOUND -> Type.MAP;
            default -> Type.UNKNOWN;
        };
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
    public Tag toBlob(ByteBuffer object) {
        if(object.hasArray()) {
            return new ByteArrayTag(object.array());
        }

        try(OutputStream os = new ByteArrayOutputStream();
            InputStream is = new ByteBufferInputStream(object)) {

            byte[] copyBuffer = new byte[1024];
            int read;
            while((read = is.read(copyBuffer)) != -1) {
                os.write(copyBuffer, 0, read);
            }

            return new ByteArrayTag(copyBuffer);

        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to copy blob!", ex);
        }
    }

    @Override
    public Tag toList(Collection<Tag> list) {
        if(list == null) return null;
        if (list.isEmpty()) {
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
        map.forEach((k,v) -> {
            if(v != null) tag.put(k,v);
        });
        return tag;
    }

    @Override
    public Tag nullValue() {
        return EndTag.INSTANCE;
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
        if(value != null) ((CompoundTag) object).put(key, value);
        return object;
    }

    @Override
    public boolean supportsMeta(Tag tag) {
        return false;
    }

    @Override
    public String getMetaProperty(Tag tag, String s) {
        return null;
    }

    @Override
    public void setMetaProperty(Tag tag, String s, String s1) {

    }
}
