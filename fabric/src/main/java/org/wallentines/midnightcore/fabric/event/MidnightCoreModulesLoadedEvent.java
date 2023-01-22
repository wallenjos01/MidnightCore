package org.wallentines.midnightcore.fabric.event;

import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleManager;

public class MidnightCoreModulesLoadedEvent extends Event {

    private final MidnightCoreAPI api;
    private final ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> moduleManager;
    private final MinecraftServer server;

    public MidnightCoreModulesLoadedEvent(MidnightCoreAPI api, ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> moduleRegistry, MinecraftServer server) {
        this.api = api;
        this.moduleManager = moduleRegistry;
        this.server = server;
    }

    public MidnightCoreAPI getAPI() {
        return api;
    }

    public ModuleManager<MidnightCoreAPI, Module<MidnightCoreAPI>> getModuleManager() {
        return moduleManager;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
