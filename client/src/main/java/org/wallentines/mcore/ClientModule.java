package org.wallentines.mcore;

import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;


public interface ClientModule extends Module<Client> {

    Registry<ModuleInfo<Client, ClientModule>> REGISTRY = new Registry<>(MidnightCoreAPI.MOD_ID);

}
