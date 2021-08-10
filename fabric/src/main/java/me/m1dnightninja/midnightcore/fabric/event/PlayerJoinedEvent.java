package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PlayerJoinedEvent extends Event {

    private final ServerPlayer player;

    public PlayerJoinedEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
