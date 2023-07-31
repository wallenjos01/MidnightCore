package org.wallentines.mcore.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ClientCustomPayloadEvent {

    private final ResourceLocation packetId;
    private final FriendlyByteBuf data;
    private boolean handled = false;

    public ClientCustomPayloadEvent(ResourceLocation packetId, FriendlyByteBuf data) {
        this.packetId = packetId;
        this.data = data;
    }

    public ResourceLocation getPacketId() {
        return packetId;
    }

    public FriendlyByteBuf getData() {
        return data;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
