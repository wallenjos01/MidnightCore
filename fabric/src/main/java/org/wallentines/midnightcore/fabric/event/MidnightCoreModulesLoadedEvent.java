package org.wallentines.midnightcore.fabric.event;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;

public class MidnightCoreModulesLoadedEvent extends Event {

    private final MidnightCoreAPI api;
    private final ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> moduleManager;

    public MidnightCoreModulesLoadedEvent(MidnightCoreAPI api, ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> moduleRegistry) {
        this.api = api;
        this.moduleManager = moduleRegistry;
    }

    public MidnightCoreAPI getAPI() {
        return api;
    }

    public ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> getModuleManager() {
        return moduleManager;
    }
}
