package org.wallentines.mcore;

import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.module.ModuleManager;
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
     * Loads all client modules using the client's module config
     */
    default void loadModules() {

        ModuleUtil.loadModules(getModuleManager(), getModuleConfig());
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

    static void registerPlaceholders(PlaceholderManager manager) {

        manager.registerSupplier("client_modules_loaded", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Client.class, srv -> srv.getModuleManager().getCount() + "", "0")));
        manager.registerSupplier("client_modules_registered", PlaceholderSupplier.inline(ctx -> ClientModule.REGISTRY.getSize() + ""));
        manager.registerSupplier("client_config_dir", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Client.class, srv -> srv.getConfigDirectory().toString(), "")));

    }

}
