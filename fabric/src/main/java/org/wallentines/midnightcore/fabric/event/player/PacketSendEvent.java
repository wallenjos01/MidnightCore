package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class PacketSendEvent extends Event {

    private final ServerPlayer entity;
    private final Packet<?> packet;

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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
