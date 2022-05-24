package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.wallentines.midnightlib.event.Event;

public class PacketHandleEvent extends Event {

    private final Packet<?> packet;
    private final PacketListener listener;

    public PacketHandleEvent(Packet<?> packet, PacketListener listener) {
        this.packet = packet;
        this.listener = listener;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public PacketListener getListener() {
        return listener;
    }
}
