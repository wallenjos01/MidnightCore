package org.wallentines.mcore.util;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;


/**
 * A utility class with module editing functions
 */
public class ModuleUtil {

    /**
     * Loads all modules from the given registry into the given manager, while passing in the given data. Reads configs
     * from the given file and saves the file afterward
     * @param manager The module manager to load modules into
     * @param moduleConfig The module configuration file
     * @param <T> The type of data to pass
     * @param <M> The type of module to load
     */
    public static <T,M extends Module<T>> void loadModules(ModuleManager<T, M> manager, FileWrapper<ConfigObject> moduleConfig) {

        manager.loadAll(moduleConfig.getRoot().asSection());
        moduleConfig.save();
    }

    /**
     * Loads a single module defined by the given module info into the given module manager while passing in the given
     * data. Reads configuration from the given file and saves the file after loading
     * @param manager The module manager to load modules into
     * @param id The module ID
     * @param moduleConfig The module configuration file
     * @param <T> The type of data to pass
     * @param <M> The type of module to load
     */
    public static <T, M extends Module<T>> boolean loadModule(ModuleManager<T, M> manager, Identifier id, FileWrapper<ConfigObject> moduleConfig) {

        ConfigObject config = moduleConfig.getRoot().asSection().get(id.toString());
        ConfigSection defaultConfig = manager.getModuleInfo(id).getDefaultConfig();
        if(config == null || !config.isSection()) { 
            config = defaultConfig;
        } else {
            config.asSection().fill(defaultConfig);
        }
        if(manager.loadModule(id, config.asSection())) {
            moduleConfig.getRoot().asSection().set(id.toString(), config);
            moduleConfig.save();
            return true;
        }
        return false;
    }

    /**
     * Reloads a single module defined by the given module info into the given module manager while passing in the given
     * data. Reads configuration from the given file and saves the file after loading
     * @param manager The module manager to load modules into
     * @param id The module ID
     * @param moduleConfig The module configuration file
     * @param <T> The type of data to pass
     * @param <M> The type of module to load
     */
    public static <T, M extends Module<T>> boolean reloadModule(ModuleManager<T, M> manager, Identifier id, FileWrapper<ConfigObject> moduleConfig) {

        manager.unloadModule(id);
        return loadModule(manager, id, moduleConfig);
    }

}
