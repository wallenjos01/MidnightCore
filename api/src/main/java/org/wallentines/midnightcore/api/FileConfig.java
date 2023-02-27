package org.wallentines.midnightcore.api;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.*;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.io.File;

public class FileConfig extends FileWrapper<ConfigObject> {

    public static final FileCodecRegistry REGISTRY = new FileCodecRegistry();

    public FileConfig(FileCodec codec, File file) {
        super(ConfigContext.INSTANCE, codec, file);
    }

    private FileConfig(FileCodec codec, File file, ConfigSection root) {
        super(ConfigContext.INSTANCE, codec, file);
        this.setRoot(root);
    }

    public static FileConfig findOrCreate(String prefix, File folder) {
        return findOrCreate(prefix, folder, new ConfigSection());
    }

    public static FileConfig findOrCreate(String prefix, File folder, ConfigSection defaults) {

        FileWrapper<ConfigObject> out = REGISTRY.findOrCreate(ConfigContext.INSTANCE, prefix, folder, defaults);
        return fromWrapper(out);
    }

    private static FileConfig fromWrapper(FileWrapper<ConfigObject> wrapper) {

        if(wrapper.getRoot() == null || !wrapper.getRoot().isSection()) {
            wrapper.setRoot(new ConfigSection());
        }

        return new FileConfig(wrapper.getCodec(), wrapper.getFile(), wrapper.getRoot().asSection());
    }

    @Override
    public ConfigSection getRoot() {
        return super.getRoot().asSection();
    }

    public static FileWrapper<ConfigObject> find(String prefix, File folder) {

        FileWrapper<ConfigObject> wrapper = REGISTRY.find(ConfigContext.INSTANCE, prefix, folder);
        if(wrapper == null) return null;

        try {
            wrapper.load();
        } catch (DecodeException ex) {
            MidnightCoreAPI.getLogger().warn("An error occurred while parsing a file!");
            ex.printStackTrace();
            wrapper.setRoot(new ConfigSection());
        }

        return fromWrapper(wrapper);
    }

    static {

        REGISTRY.registerFileCodec(JSONCodec.fileCodec());
    }

}
