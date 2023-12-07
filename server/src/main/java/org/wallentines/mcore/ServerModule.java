package org.wallentines.mcore;

import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

public interface ServerModule extends Module<Server> {

    Registry<ModuleInfo<Server, ServerModule>> REGISTRY = new Registry<>(MidnightCoreAPI.MOD_ID);

}
