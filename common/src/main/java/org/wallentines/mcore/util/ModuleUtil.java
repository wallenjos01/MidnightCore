package org.wallentines.mcore.util;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;


/**
 * A utility class with module editing functions
 */
public class ModuleUtil {

    /**
     * Loads all modules from the given registry into the given manager, while passing in the given data. Reads configs
     * from the given file and saves the file afterward
     * @param manager The module manager to load modules into
     * @param registry The module registry to read
     * @param data The data to pass into module initializers
     * @param moduleConfig The module configuration file
     * @param <T> The type of data to pass
     * @param <M> The type of module to load
     */
    public static <T,M extends Module<T>> void loadModules(ModuleManager<T, M> manager, Registry<Identifier, ModuleInfo<T, M>> registry, T data, FileWrapper<ConfigObject> moduleConfig) {

        manager.loadAll(moduleConfig.getRoot().asSection(), data, registry);
        moduleConfig.save();
    }

    /**
     * Loads a single module defined by the given module info into the given module manager while passing in the given
     * data. Reads configuration from the given file and saves the file after loading
     * @param manager The module manager to load modules into
     * @param info The module info to load
     * @param data The data to pass into module initializers
     * @param moduleConfig The module configuration file
     * @param <T> The type of data to pass
     * @param <M> The type of module to load
     */
    public static <T, M extends Module<T>> boolean loadModule(ModuleManager<T, M> manager, ModuleInfo<T, M> info, T data, FileWrapper<ConfigObject> moduleConfig) {

        ConfigObject config = moduleConfig.getRoot().asSection().get(info.getId().toString());
        if(config == null || !config.isSection()) config = info.getDefaultConfig();
        if(manager.loadModule(info, data, config.asSection())) {
            moduleConfig.getRoot().asSection().set(info.getId().toString(), config);
            moduleConfig.save();
            return true;
        }
        return false;
    }

    /**
     * Reloads a single module defined by the given module info into the given module manager while passing in the given
     * data. Reads configuration from the given file and saves the file after loading
     * @param manager The module manager to load modules into
     * @param info The module info to load
     * @param data The data to pass into module initializers
     * @param moduleConfig The module configuration file
     * @param <T> The type of data to pass
     * @param <M> The type of module to load
     */
    public static <T, M extends Module<T>> boolean reloadModule(ModuleManager<T, M> manager, ModuleInfo<T, M> info, T data, FileWrapper<ConfigObject> moduleConfig) {

        manager.unloadModule(info.getId());
        return loadModule(manager, info, data, moduleConfig);
    }

}
