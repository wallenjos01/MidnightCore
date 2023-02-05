package org.wallentines.midnightcore.spigot.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.wallentines.mdcfg.codec.Codec;
import org.wallentines.mdcfg.codec.FileCodec;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.midnightcore.api.MidnightCoreAPI;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class YamlCodec implements Codec {

    public static final YamlCodec INSTANCE = new YamlCodec();
    public static FileCodec fileCodec() {
        return new FileCodec(INSTANCE, "yml", List.of("yaml"));
    }

    @Override
    public <T> void encode(SerializeContext<T> context, T input, OutputStream stream, Charset charset) {

        Object o = context.convert(YamlContext.INSTANCE, input);
        if(!(o instanceof FileConfiguration)) throw new IllegalStateException("Unable to convert " + input + " to a YAML configuration!");

        String out = ((FileConfiguration) o).saveToString();

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream))) {

            writer.write(out);

        } catch (IOException ex) {
            MidnightCoreAPI.getLogger().warn("An error occurred while attempting to save a YAML configuration!");
            ex.printStackTrace();
        }
    }

    @Override
    public <T> T decode(SerializeContext<T> context, InputStream stream, Charset charset) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, charset));
        return YamlContext.INSTANCE.convert(context, config);
    }
}
