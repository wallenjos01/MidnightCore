package org.wallentines.mcore.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * An Event fired when a player joins the game. More specifically: right before the join message is sent.
 */
public class PlayerJoinEvent {

    private final ServerPlayer player;
    private Component joinMessage;

    public PlayerJoinEvent(ServerPlayer player, Component joinMessage) {
        this.player = player;
        this.joinMessage = joinMessage;
    }

    /**
     * Returns the player who just joined the server
     * @return The player who joined
     */
    public ServerPlayer getPlayer() {
        return player;
    }

    /**
     * Returns the join message which is to be sent
     * @return The join message
     */
    public Component getJoinMessage() {
        return joinMessage;
    }

    /**
     * Changes the join message which is to be sent
     * @param joinMessage The new join message. If null, no message will be broadcast at all
     */
    public void setJoinMessage(Component joinMessage) {
        this.joinMessage = joinMessage;
    }
}
