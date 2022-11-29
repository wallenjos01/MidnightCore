package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.client.ClientLoginQueryEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class MixinClientLoginPacketListener {

    @Shadow @Final private Connection connection;

    @Inject(method="handleCustomQuery", at=@At("HEAD"), cancellable = true)
    private void onCustomQuery(ClientboundCustomQueryPacket packet, CallbackInfo ci) {

        MidnightCoreAPI.getLogger().warn("Received Packet " + packet.getIdentifier());

        ClientLoginQueryEvent ev = new ClientLoginQueryEvent(packet.getTransactionId(), connection, packet.getIdentifier(), packet.getData());
        Event.invoke(ev);

        if(ev.hasResponded()) ci.cancel();
    }

}
