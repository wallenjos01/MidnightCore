package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.registry.MIdentifier;

import java.util.Collection;
import java.util.Set;

public interface IModuleRegistry {

    IModule getModuleById(MIdentifier id);

    default IModule getModuleById(String id) {
        return getModuleById(MIdentifier.parseOrDefault(id, "midnightcore"));
    }

    boolean isModuleLoaded(MIdentifier id);

    default boolean isModuleLoaded(String id) {
        return isModuleLoaded(MIdentifier.parseOrDefault(id, "midnightcore"));
    }

    default boolean areAllModulesLoaded(MIdentifier... ids) {

        for(MIdentifier id : ids) {
            if(!isModuleLoaded(id)) return false;
        }
        return true;
    }

    default boolean areAllModulesLoaded(String... ids) {

        for(String id : ids) {
            if(!isModuleLoaded(id)) return false;
        }
        return true;
    }

    <T extends IModule> T getModule(Class<T> clazz);

    boolean loadModule(IModule module);

    void unloadModule(IModule module);

    Collection<MIdentifier> getLoadedModuleIds();

}
