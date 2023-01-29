package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
import org.wallentines.midnightcore.fabric.level.DynamicLevelContext;
import org.wallentines.midnightlib.event.Event;


@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Shadow public abstract void broadcastSystemMessage(Component message, boolean actionBar);


    @Inject(method = "placeNewPlayer", at=@At("HEAD"))
    private void onStartJoin(Connection connection, ServerPlayer serverPlayer, CallbackInfo ci) {

        Event.invoke(new PlayerLoginEvent(serverPlayer, serverPlayer.getGameProfile()));
    }

    @Redirect(method = "placeNewPlayer", at=@At(value = "INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void onSendMessage(PlayerList instance, Component component, boolean b, Connection conn, ServerPlayer spl) {

        PlayerJoinEvent event = new PlayerJoinEvent(spl, component);
        Event.invoke(event);

        Component comp = event.getJoinMessage();
        if(comp != null) broadcastSystemMessage(comp, b);
    }

    @Redirect(method="sendLevelInfo", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
    private ServerLevel redirectLevelInfo(MinecraftServer instance, ServerPlayer spl) {

        ServerLevel lvl = spl.getLevel();
        if(!(lvl instanceof DynamicLevelContext.DynamicLevel dl)) {
            return instance.overworld();
        }

        return instance.getLevel(dl.getRoot());
    }


}
