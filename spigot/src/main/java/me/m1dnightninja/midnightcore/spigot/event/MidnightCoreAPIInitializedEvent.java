package me.m1dnightninja.midnightcore.spigot.event;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;

public class MidnightCoreAPIInitializedEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();


    private final MidnightCore midnightCore;
    private final MidnightCoreAPI api;


    public MidnightCoreAPIInitializedEvent(MidnightCore midnightCore, MidnightCoreAPI api) {
        this.midnightCore = midnightCore;
        this.api = api;
    }


    public MidnightCore getMidnightCore() {
        return midnightCore;
    }

    public MidnightCoreAPI getAPI() {
        return api;
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
