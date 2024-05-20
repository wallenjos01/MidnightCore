package org.wallentines.mcore.adapter;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.DecodeException;
import org.wallentines.mdcfg.codec.NBTCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;

public class NbtContext {

    public static <T> ConfigSection fromMojang(TagWriter<T> tagWriter, T object) {
        try {
            try (PipedOutputStream pos = new PipedOutputStream()) {
                PipedInputStream pis = new PipedInputStream();
                pos.connect(pis);

                CompletableFuture<ConfigSection> out = CompletableFuture.supplyAsync(() -> {
                    try {
                        return new NBTCodec(true).decode(ConfigContext.INSTANCE, pis).asSection();
                    } catch (IOException | DecodeException e) {
                        MidnightCoreAPI.LOGGER.warn("An error occurred while reading NBT!", e);
                        return null;
                    }
                });
                tagWriter.writeToStream(object, pos);
                pos.close();

                return out.get();
            }
        } catch (IOException | InterruptedException | ExecutionException ex) {
            MidnightCoreAPI.LOGGER.warn("An error occurred while writing Minecraft NBT!", ex);
            return null;
        }
    }

    public static <T> T toMojang(ConfigSection tag, TagReader<T> reader) {
        try {
            try(PipedOutputStream pos = new PipedOutputStream()) {
                PipedInputStream pis = new PipedInputStream();
                pos.connect(pis);

                CompletableFuture<T> out = CompletableFuture.supplyAsync(() -> {
                    try {
                        return reader.readFromStream(pis);
                    } catch (IOException e) {
                        MidnightCoreAPI.LOGGER.warn("An error occurred while reading Minecraft NBT!", e);
                        return null;
                    }
                });

                new NBTCodec(true).encode(ConfigContext.INSTANCE, tag, pos);
                pos.close();

                return out.get();
            }
        } catch (IOException | InterruptedException | ExecutionException ex) {
            MidnightCoreAPI.LOGGER.warn("An error occurred while writing NBT!", ex);
            return null;
        }
    }

    public interface TagWriter<T> {
        void writeToStream(T object, OutputStream stream) throws IOException;
    }

    public interface TagReader<T> {
        T readFromStream(InputStream stream) throws IOException;
    }
}
