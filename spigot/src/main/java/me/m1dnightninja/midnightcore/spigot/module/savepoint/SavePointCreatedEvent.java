package me.m1dnightninja.midnightcore.spigot.module.savepoint;

import me.m1dnightninja.midnightcore.spigot.module.savepoint.SavePointModule;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SavePointCreatedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    private final Player player;
    private final SavePointModule module;
    private final SavePointModule.SavePoint savePoint;

    public SavePointCreatedEvent(Player player, SavePointModule module, SavePointModule.SavePoint savePoint) {
        this.player = player;
        this.module = module;
        this.savePoint = savePoint;
    }

    public Player getPlayer() {
        return player;
    }

    public SavePointModule getModule() {
        return module;
    }

    public SavePointModule.SavePoint getSavePoint() {
        return savePoint;
    }
}
