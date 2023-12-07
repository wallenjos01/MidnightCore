package org.wallentines.mcore;

import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.types.Singleton;

import java.io.File;
import java.nio.file.Path;

public interface Client {


    /**
     * Returns the client's module manager
     * @return the client's module manager
     */
    ModuleManager<Client, ClientModule> getModuleManager();

    /**
     * Loads all modules from the given registry using the client's module config
     * @param registry The registry to find modules in
     */
    default void loadModules(Registry<ModuleInfo<Client, ClientModule>> registry) {

        ModuleUtil.loadModules(getModuleManager(), registry, this, getModuleConfig());
    }

    /**
     * Gets the configuration file for modules
     * @return The module config
     */
    default FileWrapper<ConfigObject> getModuleConfig() {

        File moduleStorage = getConfigDirectory().resolve("MidnightCore").resolve("client").toFile();

        if(!moduleStorage.isDirectory() && !moduleStorage.mkdirs()) {
            throw new IllegalStateException("Unable to create module storage directory!");
        }

        return MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "modules", moduleStorage, new ConfigSection());
    }

    /**
     * Gets the directory where the client stores configuration files
     * @return The directory where the client stores config files
     */
    Path getConfigDirectory();


    /**
     * A singleton carrying a reference to the currently running client. This is populated as soon as the client object
     * is created and will not be reset for the lifecycle of the application
     */
    Singleton<Client> RUNNING_CLIENT = new Singleton<>();

}
