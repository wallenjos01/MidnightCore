package org.wallentines.mcore;

import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

/**
 * A module which exists on the proxy
 */
public interface ProxyModule extends Module<Proxy> {

    Registry<ModuleInfo<Proxy, ProxyModule>> REGISTRY = new Registry<>(MidnightCoreAPI.MOD_ID);

    /**
     * Attempts to register a proxy module, but will not attempt to overwrite existing modules
     * @param id The ID of the module to register
     * @param info The module info
     */
    static void tryRegister(Identifier id, ModuleInfo<Proxy, ProxyModule> info) {
        if(REGISTRY.contains(id)) return;
        REGISTRY.register(id, info);
    }

}
