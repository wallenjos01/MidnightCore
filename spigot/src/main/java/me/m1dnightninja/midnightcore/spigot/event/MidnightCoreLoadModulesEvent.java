package me.m1dnightninja.midnightcore.spigot.event;

import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.List;

public class MidnightCoreLoadModulesEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();


    private final MidnightCore midnightCore;
    private final List<IModule> modules;


    public MidnightCoreLoadModulesEvent(MidnightCore midnightCore, List<IModule> modules) {
        this.midnightCore = midnightCore;
        this.modules = modules;
    }


    public MidnightCore getMidnightCore() {
        return midnightCore;
    }

    public List<IModule> getModules() {
        return modules;
    }

    public void registerModule(IModule mod) {
        modules.add(mod);
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
