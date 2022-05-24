package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class PlayerLeaveEvent extends Event {

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
