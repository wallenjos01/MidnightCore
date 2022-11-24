package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.client.ClientCustomMessageEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Inject(method="handleCustomPayload", at=@At(value = "INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onCustomPayload(ClientboundCustomPayloadPacket packet, CallbackInfo ci) {

        ClientCustomMessageEvent ev = new ClientCustomMessageEvent(packet.getIdentifier(), packet.getData());
        Event.invoke(ev);

        if(ev.isHandled()) ci.cancel();
    }

}
