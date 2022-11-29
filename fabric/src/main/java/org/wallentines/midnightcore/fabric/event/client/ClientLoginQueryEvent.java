package org.wallentines.midnightcore.fabric.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightlib.event.Event;

@Environment(EnvType.CLIENT)
public class ClientLoginQueryEvent extends Event {

    private final int transactionId;

    private final Connection connection;
    private final ResourceLocation id;
    private final FriendlyByteBuf data;

    private boolean responded = false;

    public ClientLoginQueryEvent(int transactionId, Connection connection, ResourceLocation id, FriendlyByteBuf data) {
        this.transactionId = transactionId;
        this.connection = connection;
        this.id = id;
        this.data = data;
    }

    public ResourceLocation getId() {
        return id;
    }

    public FriendlyByteBuf getData() {
        return data;
    }

    public void respond(FriendlyByteBuf data) {

        if(responded) throw new IllegalStateException("Cannot respond to negotiation request twice!");
        connection.send(new ServerboundCustomQueryPacket(transactionId, data));

        responded = true;
    }

    public boolean hasResponded() {
        return responded;
    }
}
