package org.wallentines.mcore.util;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;

import java.io.File;

public class ModuleUtil {

    public static <T,M extends Module<T>> void loadModules(ModuleManager<T, M> manager, Registry<ModuleInfo<T, M>> registry, T data, File moduleStorage) {

        if(!moduleStorage.isDirectory() && !moduleStorage.mkdirs()) {
            MidnightCoreAPI.LOGGER.warn("Unable to create module storage directory!");
            return;
        }
        manager.unloadAll();

        FileWrapper<ConfigObject> wrapper = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "modules", moduleStorage, new ConfigSection());
        manager.loadAll(wrapper.getRoot().asSection(), data, registry);

        wrapper.save();
    }

}
