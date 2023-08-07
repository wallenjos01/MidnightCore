package org.wallentines.mcore.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.mcore.event.PlayerLeaveEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListener {

    @Shadow public ServerPlayer player;

    @Shadow @Final private MinecraftServer server;

    @Redirect(method = "onDisconnect", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void onMessageSend(PlayerList instance, Component component, boolean b) {

        PlayerLeaveEvent event = new PlayerLeaveEvent(player, component);
        server.submit(() -> Event.invoke(event));

        Component comp = event.getLeaveMessage();
        if(comp != null) instance.broadcastSystemMessage(comp, false);
    }

}
