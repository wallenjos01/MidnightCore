package org.wallentines.mcore;

import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

public interface ServerModule extends Module<Server> {

    Registry<Identifier, ModuleInfo<Server, ServerModule>> REGISTRY = Registry.create(MidnightCoreAPI.MOD_ID);

}
