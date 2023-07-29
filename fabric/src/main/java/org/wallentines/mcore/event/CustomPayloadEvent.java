package org.wallentines.mcore.event;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * An event invoked when the server receives a custom payload packet from a client
 */
public record CustomPayloadEvent(ServerPlayer sender, ResourceLocation packetId, FriendlyByteBuf data) {

}
