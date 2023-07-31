package org.wallentines.mcore;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.types.Singleton;

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

        ModuleManager<Client, ClientModule> manager = getModuleManager();
        manager.unloadAll();

        FileWrapper<ConfigObject> wrapper = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "modules", getStorageDirectory().toFile(), new ConfigSection());
        manager.loadAll(wrapper.getRoot().asSection(), this, registry);

        wrapper.save();
    }

    /**
     * Gets the directory where the client stores files
     * @return The directory where the client stores files
     */
    Path getStorageDirectory();


    /**
     * A singleton carrying a reference to the currently running client. This is populated as soon as the client object
     * is created and will not be reset for the lifecycle of the application
     */
    Singleton<Client> RUNNING_CLIENT = new Singleton<>();

}
