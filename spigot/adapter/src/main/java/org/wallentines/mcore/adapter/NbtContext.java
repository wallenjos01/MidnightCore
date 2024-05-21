package org.wallentines.mcore.adapter;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.NBTCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.*;

public class NbtContext {

    public static <T> ConfigSection fromMojang(TagWriter<T> tagWriter, T object) {

        byte[] data;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            tagWriter.writeToStream(object, bos);
            data = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("An error occurred while writing NBT!", ex);
        }

        try(ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            return new NBTCodec(true).decode(ConfigContext.INSTANCE, is).asSection();
        } catch (IOException ex) {
            throw new RuntimeException("An error occurred while converting NBT!", ex);
        }
    }

    public static <T> T toMojang(ConfigSection tag, TagReader<T> reader) {

        byte[] data;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            new NBTCodec(true).encode(ConfigContext.INSTANCE, tag, bos);
            data = bos.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException("An error occurred while writing NBT!", ex);
        }

        try(ByteArrayInputStream is = new ByteArrayInputStream(data)) {
            return reader.readFromStream(is);
        } catch (IOException ex) {
            throw new RuntimeException("An error occurred while converting NBT!", ex);
        }
    }

    public interface TagWriter<T> {
        void writeToStream(T object, OutputStream stream) throws IOException;
    }

    public interface TagReader<T> {
        T readFromStream(InputStream stream) throws IOException;
    }
}
