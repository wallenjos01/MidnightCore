package org.wallentines.mcore.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * An event fired when a player leaves the game, right before the leave message sends
 */
public class PlayerLeaveEvent {

    private final ServerPlayer player;
    private Component leaveMessage;

    public PlayerLeaveEvent(ServerPlayer player, Component leaveMessage) {
        this.player = player;
        this.leaveMessage = leaveMessage;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Component getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(Component leaveMessage) {
        this.leaveMessage = leaveMessage;
    }
}
