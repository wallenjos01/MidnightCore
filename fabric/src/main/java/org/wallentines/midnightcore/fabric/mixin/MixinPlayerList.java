package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLoginEvent;
import org.wallentines.midnightlib.event.Event;

import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Shadow public abstract void broadcastSystemMessage(Component par1, ResourceKey<ChatType> par2);

    private ServerPlayer midnight_core_currentlyLoggingIn;

    @Inject(method = "placeNewPlayer", at=@At("HEAD"))
    private void onStartJoin(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {

        serverPlayer.getServer().submit(() -> Event.invoke(new PlayerLoginEvent(serverPlayer, serverPlayer.getGameProfile())));
        midnight_core_currentlyLoggingIn = serverPlayer;
    }

    @Redirect(method = "placeNewPlayer", at=@At(value = "INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/ResourceKey;)V"))
    private void onSendMessage(PlayerList instance, Component component, ResourceKey<ChatType> resourceKey) {

        PlayerJoinEvent event = new PlayerJoinEvent(midnight_core_currentlyLoggingIn, component);
        instance.getServer().submit(() -> Event.invoke(event));

        midnight_core_currentlyLoggingIn = null;

        Component comp = event.getJoinMessage();
        if(comp != null) broadcastSystemMessage(comp, resourceKey);
    }


}
