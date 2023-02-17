package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.event.server.*;
import org.wallentines.midnightlib.event.Event;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow
    private int tickCount;

    @Shadow private PlayerList playerList;

    @Inject(method="runServer", at=@At("HEAD"))
    private void onRunServer(CallbackInfo ci) {
        MinecraftServer srv = (MinecraftServer) (Object) this;
        srv.submit(() -> Event.invoke(new ServerStartEvent(srv)));
    }

    @Inject(method = "createLevels", at = @At("TAIL"))
    private void onCreateWorlds(ChunkProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        MinecraftServer srv = (MinecraftServer) (Object) this;
        srv.submit(() -> Event.invoke(new ServerLoadWorldsEvent(srv)));
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        MinecraftServer srv = (MinecraftServer) (Object) this;
        srv.submit(() -> Event.invoke(new ServerTickEvent(srv, tickCount)));
    }

    @Inject(method="stopServer", at=@At(value="HEAD"))
    private void onShutdown(CallbackInfo ci) {
        MinecraftServer srv = (MinecraftServer) (Object) this;
        Event.invoke(new ServerStopEvent(srv));
    }

    @Inject(method="stopServer", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;saveAll()V"))
    private void onSavePlayers(CallbackInfo ci) {
        for(ServerPlayer spl : playerList.getPlayers()) {
            Event.invoke(new PlayerLeaveEvent(spl, Component.empty()));
        }
    }

    @Inject(method="stopServer", at=@At(value="RETURN"))
    private void onStopped(CallbackInfo ci) {
        MinecraftServer srv = (MinecraftServer) (Object) this;
        Event.invoke(new ServerStoppedEvent(srv));
    }
}

