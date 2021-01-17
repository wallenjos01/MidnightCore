package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;

public class PlayerFoodLevelChangeEvent extends Event {

    private final ServerPlayer player;
    private final int previousFoodLevel;
    private int newFoodLevel;

    private boolean cancelled = false;

    public PlayerFoodLevelChangeEvent(ServerPlayer player, int previousFoodLevel, int newFoodLevel) {
        this.player = player;
        this.previousFoodLevel = previousFoodLevel;
        this.newFoodLevel = newFoodLevel;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getPreviousFoodLevel() {
        return previousFoodLevel;
    }

    public int getNewFoodLevel() {
        return newFoodLevel;
    }

    public void setNewFoodLevel(int newFoodLevel) {
        this.newFoodLevel = newFoodLevel;
    }


    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
