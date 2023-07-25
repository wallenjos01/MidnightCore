package org.wallentines.mcore.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.mcore.event.PlayerJoinEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Shadow public abstract void broadcastSystemMessage(Component component, boolean bl);

    @Redirect(method = "placeNewPlayer", at=@At(value = "INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void onSendMessage(PlayerList instance, Component component, boolean b, Connection conn, ServerPlayer spl) {

        PlayerJoinEvent event = new PlayerJoinEvent(spl, component);
        Event.invoke(event);

        Component comp = event.getJoinMessage();
        if(comp != null) {
            broadcastSystemMessage(comp, b);
        }
    }

}
