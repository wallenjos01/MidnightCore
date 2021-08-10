package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.fabric.event.PlayerJoinedEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.event.PlayerJoinEvent;
import me.m1dnightninja.midnightcore.fabric.event.PlayerLoginEvent;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class MixinPlayerManager {

    ServerPlayer joining = null;

    @Inject(method = "placeNewPlayer", at = @At("HEAD"))
    private void onConnect(Connection connection, ServerPlayer player, CallbackInfo ci) {
        Event.invoke(new PlayerLoginEvent(player, player.getGameProfile()));

        joining = player;
    }

    @ModifyArg(method = "placeNewPlayer", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), index = 0)
    private Component onJoin(Component joinMessage) {

        PlayerJoinEvent ev = new PlayerJoinEvent(joining, joinMessage);
        Event.invoke(ev);

        joining = null;

        return ev.getJoinMessage();
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onConnected(Connection connection, ServerPlayer player, CallbackInfo ci) {
        Event.invoke(new PlayerJoinedEvent(player));
    }

    @Inject(method="remove", at=@At("HEAD"))
    public void onQuit(final ServerPlayer ent, final CallbackInfo info) {

        Event.invoke(new PlayerDisconnectEvent(ent));
        MidnightCoreAPI.getInstance().getPlayerManager().cleanupPlayer(ent.getUUID());
    }

}
