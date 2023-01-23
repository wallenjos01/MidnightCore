package org.wallentines.midnightcore.spigot.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

@SuppressWarnings("unused")
public class MidnightCoreLoadModulesEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    private final MidnightCoreAPI api;
    private final Registry<ModuleInfo<MServer, ServerModule>> registry;

    public MidnightCoreLoadModulesEvent(MidnightCoreAPI api, Registry<ModuleInfo<MServer, ServerModule>> registry) {
        this.api = api;
        this.registry = registry;
    }

    public MidnightCoreAPI getApi() {
        return api;
    }

    public Registry<ModuleInfo<MServer, ServerModule>> getRegistry() {
        return registry;
    }
}
