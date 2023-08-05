package org.wallentines.mcore;

import org.wallentines.mcore.util.ModuleUtil;
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

        File moduleStorage = getConfigDirectory().resolve("MidnightCore").resolve("client").toFile();
        ModuleUtil.loadModules(getModuleManager(), registry, this, moduleStorage);
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
