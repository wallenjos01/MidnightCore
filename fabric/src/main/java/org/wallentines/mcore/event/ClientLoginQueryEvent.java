package org.wallentines.mcore.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ClientLoginQueryEvent {

    private final ResourceLocation packetId;
    private final FriendlyByteBuf data;
    private FriendlyByteBuf response;

    public ClientLoginQueryEvent(ResourceLocation packetId, FriendlyByteBuf data) {
        this.packetId = packetId;
        this.data = data;
    }

    public ResourceLocation getPacketId() {
        return packetId;
    }

    public FriendlyByteBuf getData() {
        return data;
    }

    public FriendlyByteBuf getResponse() {
        return response;
    }

    public void setResponse(FriendlyByteBuf response) {
        this.response = response;
    }
}
