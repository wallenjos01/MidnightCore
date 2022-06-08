package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;


public class PlayerChatEvent extends Event {

    private final ServerPlayer player;
    private final String message;
    private boolean cancelled = false;

    public PlayerChatEvent(ServerPlayer player, String message) {
        this.player = player;
        this.message = message;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getMessage() {
        return message;
    }


    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
