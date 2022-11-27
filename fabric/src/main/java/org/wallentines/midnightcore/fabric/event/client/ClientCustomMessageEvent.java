package org.wallentines.midnightcore.fabric.event.client;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightlib.event.Event;

public class ClientCustomMessageEvent extends Event {

    private final ClientPacketListener listener;
    private final ResourceLocation id;
    private final FriendlyByteBuf data;

    private boolean handled = false;

    public ClientCustomMessageEvent(ClientPacketListener listener, ResourceLocation id, FriendlyByteBuf data) {
        this.listener = listener;
        this.id = id;
        this.data = data;
    }

    public ResourceLocation getId() {
        return id;
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

    public void respond(FriendlyByteBuf response) {

        listener.send(new ServerboundCustomPayloadPacket(id, response));
    }
}
