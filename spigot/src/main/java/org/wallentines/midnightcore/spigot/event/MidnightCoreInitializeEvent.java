package org.wallentines.midnightcore.spigot.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Registry;

public class MidnightCoreInitializeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private final MidnightCoreAPI api;

    public MidnightCoreInitializeEvent(MidnightCoreAPI api) {
        this.api = api;
    }

    public MidnightCoreAPI getApi() {
        return api;
    }
}
