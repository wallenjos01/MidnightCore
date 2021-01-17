package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PlayerJoinEvent extends Event {

    private final ServerPlayer ent;
    private Component joinMessage;

    public PlayerJoinEvent(ServerPlayer ent, Component joinMessage) {
        this.ent = ent;
        this.joinMessage = joinMessage;
    }

    public ServerPlayer getPlayer() {
        return ent;
    }

    public Component getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(Component joinMessage) {
        this.joinMessage = joinMessage;
    }
}
