package me.m1dnightninja.midnightcore.fabric.mixin;

import com.mojang.datafixers.util.Either;
import me.m1dnightninja.midnightcore.fabric.event.ChunkLoadEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Mixin(ChunkStatus.class)
public class MixinChunkStatus {

    @Inject(method = "load(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureManager;Lnet/minecraft/server/level/ThreadedLevelLightEngine;Ljava/util/function/Function;Lnet/minecraft/world/level/chunk/ChunkAccess;)Ljava/util/concurrent/CompletableFuture;", at=@At("RETURN"))
    private void onLoad(ServerLevel serverLevel, StructureManager structureManager, ThreadedLevelLightEngine threadedLevelLightEngine, Function<ChunkAccess, CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> function, ChunkAccess chunkAccess, CallbackInfoReturnable<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> cir) {

        Event.invoke(new ChunkLoadEvent(chunkAccess.getPos(), serverLevel));
    }

}
