package org.wallentines.mcore.adapter;

import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.NBTWriter;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class NbtContext implements SerializeContext<Object> {

    public static final NbtContext INSTANCE = new NbtContext();

    @Override
    public String asString(Object object) {
        return object instanceof String ? (String) object : null;
    }

    @Override
    public Number asNumber(Object object) {
        return object instanceof Number ? (Number) object : null;
    }

    @Override
    public Boolean asBoolean(Object object) {
        return object instanceof Number ? ((Number) object).byteValue() != 0 : null;
    }

    @Override
    public Collection<Object> asList(Object object) {

        if(object instanceof int[]) {
            List<Object> out = new ArrayList<>();
            for(int i : (int[]) object) {
                out.add(i);
            }
            return out;
        }
        if(object instanceof byte[]) {
            List<Object> out = new ArrayList<>();
            for(byte i : (byte[]) object) {
                out.add(i);
            }
            return out;
        }
        if(object instanceof long[]) {
            List<Object> out = new ArrayList<>();
            for(long i : (long[]) object) {
                out.add(i);
            }
            return out;
        }

        return object instanceof NBTList ? (NBTList) object : null;
    }

    @Override
    public Map<String, Object> asMap(Object object) {
        return object instanceof NBTCompound ? (NBTCompound) object : null;
    }

    @Override
    public Map<String, Object> asOrderedMap(Object object) {
        return asMap(object);
    }

    @Override
    public boolean isString(Object object) {
        return object instanceof String;
    }

    @Override
    public boolean isNumber(Object object) {
        return object instanceof Number;
    }

    @Override
    public boolean isBoolean(Object object) {
        return object instanceof Number;
    }

    @Override
    public boolean isList(Object object) {
        return object instanceof NBTList ||
                object instanceof int[] ||
                object instanceof byte[] ||
                object instanceof long[];
    }

    @Override
    public boolean isMap(Object object) {
        return object instanceof NBTCompound;
    }

    @Override
    public Collection<String> getOrderedKeys(Object object) {
        if(!isMap(object)) return null;
        return ((NBTCompound) object).keySet();
    }

    @Override
    public Object get(String key, Object object) {
        if(!isMap(object)) return null;
        return ((NBTCompound) object).get(key);
    }

    @Override
    public Object toString(String object) {
        return object;
    }

    @Override
    public Object toNumber(Number object) {
        return object;
    }

    @Override
    public Object toBoolean(Boolean object) {
        return object ? (byte) 1 : (byte) 0;
    }

    @Override
    public Object toList(Collection<Object> list) {

        if(list.isEmpty()) {
            return new NBTList(TagType.END);
        }

        if(list.stream().allMatch(tag -> tag instanceof Integer)) {

            int[] out = new int[list.size()];
            int index = 0;
            for(Object o : list) {
                out[index++] = (Integer) o;
            }
            return out;
        }
        if(list.stream().allMatch(tag -> tag instanceof Byte)) {
            byte[] out = new byte[list.size()];
            int index = 0;
            for(Object o : list) {
                out[index++] = (Byte) o;
            }
            return out;
        }
        if(list.stream().allMatch(tag -> tag instanceof Long)) {
            long[] out = new long[list.size()];
            int index = 0;
            for(Object o : list) {
                out[index++] = (Byte) o;
            }
            return out;
        }


        NBTList out = new NBTList(TagType.fromObject(list.iterator().next()));
        out.addAll(list);

        return out;
    }

    @Override
    public Object toMap(Map<String, Object> map) {

        NBTCompound out = new NBTCompound();
        out.putAll(map);
        return out;
    }

    @Override
    public Object set(String key, Object value, Object object) {
        if(!isMap(object)) return null;

        ((NBTCompound) object).put(key, value);
        return object;
    }

    public static <T> NBTCompound fromMojang(TagWriter<T> tagWriter, T object) {
        try {
            try (PipedOutputStream pos = new PipedOutputStream()) {
                PipedInputStream pis = new PipedInputStream();
                pos.connect(pis);

                CompletableFuture<NBTCompound> out = CompletableFuture.supplyAsync(() -> {
                    try {
                        return NBTReader.read(pis);
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

    public static <T> T toMojang(NBTCompound tag, TagReader<T> reader) {
        try {
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

                NBTWriter.write(tag, new DataOutputStream(pos));
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
