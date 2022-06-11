package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.event.player.PacketHandleEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(Connection.class)
public class MixinConnection {

    @Inject(method="genericsFtw", at=@At("HEAD"))
    private static void onHandle(Packet<?> packet, PacketListener packetListener, CallbackInfo ci) {

        MinecraftServer server = MidnightCore.getInstance().getServer();
        if(server != null) server.submit(() -> Event.invoke(new PacketHandleEvent(packet, packetListener)));
    }

}
