package org.wallentines.midnightcore.api;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.RequirementType;

public final class Registries {

    public static final Registry<ModuleInfo<MServer, ServerModule>> MODULE_REGISTRY = new Registry<>(MidnightCoreAPI.DEFAULT_NAMESPACE);
    public static final Registry<RequirementType<MPlayer>> REQUIREMENT_REGISTRY = new Registry<>(MidnightCoreAPI.DEFAULT_NAMESPACE);

}
