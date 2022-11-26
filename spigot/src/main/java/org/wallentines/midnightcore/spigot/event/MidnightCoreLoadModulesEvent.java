package org.wallentines.midnightcore.spigot.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

public class MidnightCoreLoadModulesEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private final MidnightCoreAPI api;
    private final Registry<ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>> registry;

    public MidnightCoreLoadModulesEvent(MidnightCoreAPI api, Registry<ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>> registry) {
        this.api = api;
        this.registry = registry;
    }

    public MidnightCoreAPI getApi() {
        return api;
    }

    public Registry<ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>>> getRegistry() {
        return registry;
    }
}
