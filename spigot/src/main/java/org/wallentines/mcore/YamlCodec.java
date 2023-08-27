package org.wallentines.mcore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.codec.Codec;
import org.wallentines.mdcfg.codec.EncodeException;
import org.wallentines.mdcfg.codec.FileCodec;
import org.wallentines.mdcfg.serializer.SerializeContext;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class YamlCodec implements Codec {

    public static final YamlCodec INSTANCE = new YamlCodec();
    public static FileCodec fileCodec() {
        return new FileCodec(INSTANCE, "yml", List.of("yaml"));
    }

    @Override
    public <T> void encode(SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws IOException {

        Object o = context.convert(YamlContext.INSTANCE, input);
        if(!(o instanceof FileConfiguration)) throw new EncodeException("Unable to convert " + input + " to a YAML configuration!");

        String out = ((FileConfiguration) o).saveToString();

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {
            writer.write(out);
        }
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, charset));
        return YamlContext.INSTANCE.convert(context, config);
    }
}
