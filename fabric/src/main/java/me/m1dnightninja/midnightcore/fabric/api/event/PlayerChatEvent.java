package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PlayerChatEvent extends Event {

    private final ServerPlayer player;
    private String message;
    private Component broadcast;

    private boolean changed;

    private boolean cancelled = false;

    public PlayerChatEvent(ServerPlayer player, String message, Component broadcast) {
        this.player = player;
        this.message = message;
        this.broadcast = broadcast;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Component getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(Component broadcast) {
        this.changed = true;
        this.broadcast = broadcast;
    }

    public boolean wasMessageChanged() {
        return changed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
