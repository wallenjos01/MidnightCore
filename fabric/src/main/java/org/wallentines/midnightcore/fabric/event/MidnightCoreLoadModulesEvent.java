package org.wallentines.midnightcore.fabric.event;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

public class MidnightCoreLoadModulesEvent extends Event {

    private final MidnightCoreAPI api;
    private final Registry<ModuleInfo<MidnightCoreAPI>> moduleRegistry;

    public MidnightCoreLoadModulesEvent(MidnightCoreAPI api, Registry<ModuleInfo<MidnightCoreAPI>> moduleRegistry) {
        this.api = api;
        this.moduleRegistry = moduleRegistry;
    }

    public MidnightCoreAPI getAPI() {
        return api;
    }

    public Registry<ModuleInfo<MidnightCoreAPI>> getModuleRegistry() {
        return moduleRegistry;
    }
}
