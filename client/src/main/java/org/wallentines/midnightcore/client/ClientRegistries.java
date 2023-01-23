package org.wallentines.midnightcore.client;

import org.wallentines.midnightcore.client.module.ClientModule;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

public class ClientRegistries {

    public static final Registry<ModuleInfo<MidnightCoreClient, ClientModule>> CLIENT_MODULE_REGISTRY = new Registry<>();

}
