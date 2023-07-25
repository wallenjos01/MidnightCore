package org.wallentines.mcore.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PlayerJoinEvent {

    private final ServerPlayer player;
    private final Component joinMessage;

    public PlayerJoinEvent(ServerPlayer player, Component joinMessage) {
        this.player = player;
        this.joinMessage = joinMessage;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Component getJoinMessage() {
        return joinMessage;
    }
}
