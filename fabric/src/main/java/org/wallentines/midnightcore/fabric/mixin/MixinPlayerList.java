package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.wallentines.midnightcore.fabric.event.player.PlayerJoinEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerLoginEvent;
import org.wallentines.midnightcore.fabric.event.player.PlayerRespawnEvent;
import org.wallentines.midnightlib.event.Event;

import java.util.Optional;


@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Shadow public abstract void broadcastSystemMessage(Component message, boolean actionBar);

    @Unique
    private PlayerRespawnEvent mcore$lastRespawn;


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

    @Inject(method="respawn", at=@At(value="INVOKE", target = "Lnet/minecraft/world/level/Level;getLevelData()Lnet/minecraft/world/level/storage/LevelData;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onRespawn(ServerPlayer serverPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir, BlockPos blockPos, float f, boolean bl2, ServerLevel serverLevel, Optional optional, ServerLevel serverLevel2, ServerPlayer serverPlayer2, boolean bl3, byte b) {

        mcore$lastRespawn = new PlayerRespawnEvent(serverPlayer2, serverPlayer2.position(), serverPlayer2.getLevel());
        Event.invoke(mcore$lastRespawn);

        serverPlayer2.setPos(mcore$lastRespawn.getPosition());
        serverPlayer2.setLevel(mcore$lastRespawn.getLevel());
    }

    @ModifyArgs(method="respawn", at=@At(value="INVOKE", target="Lnet/minecraft/network/protocol/game/ClientboundSetDefaultSpawnPositionPacket;<init>(Lnet/minecraft/core/BlockPos;F)V"))
    private void redirectRespawnPosition(Args args) {
        args.set(0, mcore$lastRespawn.getLevel().getSharedSpawnPos());
        args.set(1, mcore$lastRespawn.getLevel().getSharedSpawnAngle());
    }

    @ModifyArg(method="respawn", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V"))
    private ServerLevel redirectRespawnLevelInfo(ServerLevel orig) {
        return mcore$lastRespawn.getLevel();
    }

    @Redirect(method="respawn", at=@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerLevel;addRespawnedPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void redirectAddPlayer(ServerLevel level, ServerPlayer player) {
        mcore$lastRespawn.getLevel().addRespawnedPlayer(player);
    }

    @Inject(method="respawn", at=@At("RETURN"))
    private void afterRespawn(ServerPlayer serverPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        mcore$lastRespawn = null;
    }
}
