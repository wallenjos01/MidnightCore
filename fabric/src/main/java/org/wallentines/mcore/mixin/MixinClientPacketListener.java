package org.wallentines.mcore.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.mcore.event.ClientCustomPayloadEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    @Redirect(method="handleCustomPayload", at=@At(value="INVOKE", target="Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private void onUnknown(Logger instance, String message, Object obj, ClientboundCustomPayloadPacket packet) {

        ClientCustomPayloadEvent ev = new ClientCustomPayloadEvent(packet.getIdentifier(), packet.getData());
        Event.invoke(ev);

        if(!ev.isHandled()) {
            instance.warn(message, obj);
        }
    }

}
