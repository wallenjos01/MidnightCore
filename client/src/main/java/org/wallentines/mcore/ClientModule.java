package org.wallentines.mcore;

import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

/**
 * A module which is loaded on the client
 */
public interface ClientModule extends Module<Client> {

    /**
     * A registry for defining client module information. The client will attempt to load all modules in this registry
     * unless they are disabled by the user
     */
    Registry<Identifier, ModuleInfo<Client, ClientModule>> REGISTRY = Registry.create(MidnightCoreAPI.MOD_ID);

}
