package org.wallentines.mcore;

import org.jetbrains.annotations.NotNull;
import org.wallentines.mdcfg.codec.Codec;
import org.wallentines.mdcfg.codec.FileCodec;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class YamlCodec implements Codec {

    public static final YamlCodec INSTANCE = new YamlCodec();
    public static FileCodec fileCodec() {
        return new FileCodec(INSTANCE, "yml", List.of("yaml"));
    }

    private final Yaml yaml;

    public YamlCodec() {
        this(new Yaml());
    }

    public YamlCodec(Yaml yaml) {
        this.yaml = yaml;
    }


    @Override
    public <T> void encode(SerializeContext<T> context, T input, @NotNull OutputStream stream, Charset charset) throws IOException {

        try(OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            yaml.dump(context.convert(YamlContext.INSTANCE, input), writer);
        }
    }

    @Override
    public <T> T decode(@NotNull SerializeContext<T> context, @NotNull InputStream stream, Charset charset) {

        return YamlContext.INSTANCE.convert(context, yaml.load(new InputStreamReader(stream, charset)));
    }
}
