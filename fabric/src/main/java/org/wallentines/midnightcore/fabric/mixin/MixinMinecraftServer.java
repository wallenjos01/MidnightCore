package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.server.ServerLoadWorldsEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStartEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerTickEvent;
import org.wallentines.midnightlib.event.Event;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow
    private int tickCount;

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

    @Inject(method="stopServer", at=@At("HEAD"))
    private void onShutdown(CallbackInfo ci) {
        MinecraftServer srv = (MinecraftServer) (Object) this;
        srv.submit(() -> Event.invoke(new ServerStopEvent(srv)));
    }

}
