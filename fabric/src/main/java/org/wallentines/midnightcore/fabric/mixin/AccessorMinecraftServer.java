package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(MinecraftServer.class)
public interface AccessorMinecraftServer {

    @Invoker("setInitialSpawn")
    static void callSetInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
        throw new UnsupportedOperationException();
    }

    @Invoker("setupDebugLevel")
    void callSetupDebugLevel(WorldData worldData);

    @Accessor("levels")
    Map<ResourceKey<Level>, ServerLevel> getLevels();

    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess getStorageSource();

    @Accessor("progressListenerFactory")
    ChunkProgressListenerFactory getProgressListenerFactory();

}
