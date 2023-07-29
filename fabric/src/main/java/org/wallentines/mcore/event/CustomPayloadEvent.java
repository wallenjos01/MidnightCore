package org.wallentines.mcore.event;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * An event invoked when the server receives a custom payload packet from a client
 */
public class CustomPayloadEvent {

    private final ServerPlayer sender;
    private final ResourceLocation packetId;
    private final FriendlyByteBuf data;

    public CustomPayloadEvent(ServerPlayer sender, ResourceLocation packetId, FriendlyByteBuf data) {
        this.sender = sender;
        this.packetId = packetId;
        this.data = data;
    }

    public ServerPlayer getSender() {
        return sender;
    }

    public ResourceLocation getPacketId() {
        return packetId;
    }

    public FriendlyByteBuf getData() {
        return data;
    }
}
