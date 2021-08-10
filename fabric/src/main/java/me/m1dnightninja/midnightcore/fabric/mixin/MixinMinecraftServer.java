package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.event.ServerLoadWorldsEvent;
import me.m1dnightninja.midnightcore.fabric.event.ServerTickEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Shadow private int tickCount;

    @Inject(method = "createLevels", at = @At("TAIL"))
    private void onCreateWorlds(ChunkProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        Event.invoke(new ServerLoadWorldsEvent((MinecraftServer) (Object) this));
    }

    @Inject(method = "tickServer", at = @At("TAIL"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        Event.invoke(new ServerTickEvent((MinecraftServer) (Object) this, tickCount));
    }
}
