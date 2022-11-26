package org.wallentines.midnightcore.api;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.RequirementType;

public final class Registries {

    public static final Registry<ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>> MODULE_REGISTRY = new Registry<>();
    public static final Registry<ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>> CLIENT_MODULE_REGISTRY = new Registry<>();
    public static final Registry<RequirementType<MPlayer>> REQUIREMENT_REGISTRY = new Registry<>();

}
