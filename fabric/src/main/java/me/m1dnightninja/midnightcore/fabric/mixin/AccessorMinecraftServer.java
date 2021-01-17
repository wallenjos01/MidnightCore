package me.m1dnightninja.midnightcore.fabric.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(MinecraftServer.class)
public interface AccessorMinecraftServer {

    @Invoker
    static void callSetInitialSpawn(ServerLevel world, ServerLevelData serverWorldProperties, boolean bonusChest, boolean debugWorld, boolean bl) {
        throw new UnsupportedOperationException();
    }

    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess getStorageAccess();

    @Accessor("levels")
    Map<ResourceKey<Level>, ServerLevel> getWorlds();

    @Accessor("resources")
    ServerResources getResources();

    @Accessor("progressListenerFactory")
    ChunkProgressListenerFactory getListenerFactory();

    @Invoker("waitUntilNextTick")
    void callWaitUntilNextTick();
}
