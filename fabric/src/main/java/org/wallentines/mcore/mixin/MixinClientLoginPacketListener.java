package org.wallentines.mcore.mixin;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.mcore.event.ClientLoginQueryEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ClientHandshakePacketListenerImpl.class)
public class MixinClientLoginPacketListener {

    @Redirect(method="handleCustomQuery", at=@At(value="INVOKE", target="Lnet/minecraft/network/Connection;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void onSend(Connection instance, Packet<?> packet, ClientboundCustomQueryPacket inPacket) {

        ClientLoginQueryEvent ev = new ClientLoginQueryEvent(inPacket.getIdentifier(), inPacket.getData());
        Event.invoke(ev);

        instance.send(new ServerboundCustomQueryPacket(inPacket.getTransactionId(), ev.getResponse()));
    }

}
