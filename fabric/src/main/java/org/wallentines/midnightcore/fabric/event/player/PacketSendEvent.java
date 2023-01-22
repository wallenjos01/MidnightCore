package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class PacketSendEvent extends Event {

    private final ServerPlayer entity;
    private Packet<?> packet;
    private boolean cancelled = false;

    public PacketSendEvent(ServerPlayer entity, Packet<?> packet) {
        this.entity = entity;
        this.packet = packet;
    }

    public ServerPlayer getPlayer() {
        return entity;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
