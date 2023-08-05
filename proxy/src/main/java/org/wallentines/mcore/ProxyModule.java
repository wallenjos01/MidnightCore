package org.wallentines.mcore;

import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

public interface ProxyModule extends Module<Proxy> {

    Registry<ModuleInfo<Proxy, ProxyModule>> REGISTRY = new Registry<>(MidnightCoreAPI.MOD_ID);

    static void tryRegister(Identifier id, ModuleInfo<Proxy, ProxyModule> info) {
        if(REGISTRY.contains(id)) return;
        REGISTRY.register(id, info);
    }

}
