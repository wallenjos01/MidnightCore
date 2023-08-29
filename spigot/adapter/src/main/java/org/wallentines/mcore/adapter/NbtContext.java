package org.wallentines.mcore.adapter;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.api.Tag;
import dev.dewy.nbt.tags.array.ByteArrayTag;
import dev.dewy.nbt.tags.array.IntArrayTag;
import dev.dewy.nbt.tags.array.LongArrayTag;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.*;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class NbtContext implements SerializeContext<Tag> {

    public static final NbtContext INSTANCE = new NbtContext();

    @Override
    public String asString(Tag object) {
        return object instanceof StringTag ? ((StringTag) object).getValue() : null;
    }

    @Override
    public Number asNumber(Tag object) {
        return object instanceof NumericalTag<?> ? ((NumericalTag<?>) object).getValue() : null;
    }

    @Override
    public Boolean asBoolean(Tag object) {
        return object instanceof ByteTag ? ((ByteTag) object).getValue() != 0 : null;
    }

    @Override
    public Collection<Tag> asList(Tag object) {
        if(object instanceof ListTag<?>) {
            return new ArrayList<>(((ListTag<?>) object).getValue());
        }
        ArrayList<Tag> out = new ArrayList<>();
        if(object instanceof IntArrayTag) {
            for(int i : ((IntArrayTag) object).getValue()) { out.add(new IntTag(i)); }
        } else if(object instanceof ByteArrayTag) {
            for(byte i : ((ByteArrayTag) object).getValue()) { out.add(new ByteTag(i)); }
        } else if(object instanceof LongArrayTag) {
            for(long i : ((LongArrayTag) object).getValue()) { out.add(new LongTag(i)); }
        }
        return out;
    }

    @Override
    public Map<String, Tag> asMap(Tag object) {
        if(!isMap(object)) return null;

        CompoundTag tag = (CompoundTag) object;
        Map<String, Tag> out = new HashMap<>();
        for(String key : tag.keySet()) {
            out.put(key, tag.get(key));
        }
        return out;
    }

    @Override
    public Map<String, Tag> asOrderedMap(Tag object) {
        return asMap(object);
    }

    @Override
    public boolean isString(Tag object) {
        return object instanceof StringTag;
    }

    @Override
    public boolean isNumber(Tag object) {
        return object instanceof NumericalTag<?>;
    }

    @Override
    public boolean isBoolean(Tag object) {
        return object instanceof ByteTag;
    }

    @Override
    public boolean isList(Tag object) {
        return object instanceof ListTag<?>;
    }

    @Override
    public boolean isMap(Tag object) {
        return object instanceof CompoundTag;
    }

    @Override
    public Collection<String> getOrderedKeys(Tag object) {
        if(!isMap(object)) return null;
        return ((CompoundTag) object).keySet();
    }

    @Override
    public Tag get(String key, Tag object) {
        if(!isMap(object)) return null;
        return ((CompoundTag) object).get(key);
    }

    @Override
    public Tag toString(String object) {
        return new StringTag(object);
    }

    @Override
    public Tag toNumber(Number object) {

        if(object instanceof Integer) return new IntTag(object.intValue());
        if(object instanceof Byte) return new ByteTag(object.byteValue());
        if(object instanceof Long) return new LongTag(object.longValue());
        if(object instanceof Short) return new ShortTag(object.shortValue());
        if(object instanceof Float) return new FloatTag(object.floatValue());
        if(object instanceof Double) return new DoubleTag(object.floatValue());

        return ConfigPrimitive.isInteger(object) ? new LongTag(object.longValue()) : new DoubleTag(object.doubleValue());
    }

    @Override
    public Tag toBoolean(Boolean object) {
        return new ByteTag(object ? 1 : 0);
    }

    @Override
    public Tag toList(Collection<Tag> list) {

        if(list.stream().allMatch(tag -> tag instanceof IntTag)) {
            return new IntArrayTag(list.stream().map(tag -> ((IntTag) tag).getValue()).collect(Collectors.toList()));
        }
        if(list.stream().allMatch(tag -> tag instanceof ByteTag)) {
            return new ByteArrayTag(list.stream().map(tag -> ((ByteTag) tag).getValue()).collect(Collectors.toList()));
        }
        if(list.stream().allMatch(tag -> tag instanceof LongTag)) {
            return new LongArrayTag(list.stream().map(tag -> ((LongTag) tag).getValue()).collect(Collectors.toList()));
        }

        ListTag<Tag> out = new ListTag<>();
        list.forEach(out::add);

        return out;
    }

    @Override
    public Tag toMap(Map<String, Tag> map) {

        CompoundTag out = new CompoundTag();
        for(Map.Entry<String, Tag> ent : map.entrySet()) {
            out.put(ent.getKey(), ent.getValue());
        }
        return out;
    }

    @Override
    public Tag set(String key, Tag value, Tag object) {
        if(!isMap(object)) return null;

        ((CompoundTag) object).put(key, value);
        return object;
    }

    public static <T> CompoundTag fromMojang(TagWriter<T> tagWriter, T object) {
        try {
            Nbt nbt = new Nbt();
            try (PipedOutputStream pos = new PipedOutputStream()) {
                PipedInputStream pis = new PipedInputStream();
                pos.connect(pis);

                CompletableFuture<CompoundTag> out = CompletableFuture.supplyAsync(() -> {
                    try {
                        return nbt.fromStream(new DataInputStream(pis));
                    } catch (IOException e) {
                        return null;
                    }
                });
                tagWriter.writeToStream(object, new DataOutputStream(pos));
                pos.close();

                return out.get();
            }
        } catch (IOException | InterruptedException | ExecutionException ex) {
            return null;
        }
    }

    public static <T> T toMojang(CompoundTag tag, TagReader<T> reader) {
        try {
            Nbt nbt = new Nbt();
            try(PipedOutputStream pos = new PipedOutputStream()) {
                PipedInputStream pis = new PipedInputStream();
                pos.connect(pis);

                CompletableFuture<T> out = CompletableFuture.supplyAsync(() -> {
                    try {
                        return reader.readFromStream(new DataInputStream(pis));
                    } catch (IOException e) {
                        return null;
                    }
                });

                nbt.toStream(tag, new DataOutputStream(pos));
                pos.close();

                return out.get();
            }
        } catch (IOException | InterruptedException | ExecutionException ex) {
            return null;
        }
    }

    public interface TagWriter<T> {
        void writeToStream(T object, DataOutput stream) throws IOException;
    }

    public interface TagReader<T> {
        T readFromStream(DataInputStream stream) throws IOException;
    }
}
