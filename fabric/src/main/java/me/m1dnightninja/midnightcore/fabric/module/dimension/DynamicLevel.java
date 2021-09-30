package me.m1dnightninja.midnightcore.fabric.module.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;

public class DynamicLevel extends ServerLevel {

    private final DynamicLevelStorageSource.DynamicLevelStorageAccess session;

    public DynamicLevel(MinecraftServer minecraftServer, Executor executor, DynamicLevelStorageSource.DynamicLevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, DimensionType dimensionType, ChunkProgressListener chunkProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List<CustomSpawner> list, boolean bl2) {
        super(minecraftServer, executor, levelStorageAccess, serverLevelData, resourceKey, dimensionType, chunkProgressListener, chunkGenerator, bl, l, list, bl2);

        session = levelStorageAccess;
    }

    @Nullable
    public MapItemSavedData getMapData(String string) {
        return getDataStorage().get(MapItemSavedData::load, string);
    }

    @Override
    public void setMapData(String string, MapItemSavedData mapItemSavedData) {
        getDataStorage().set(string, mapItemSavedData);
    }

    @Override
    public int getFreeMapId() {
        return getDataStorage().computeIfAbsent(MapIndex::load, MapIndex::new, "idcounts").getFreeAuxValueForMap();
    }

    @Override
    public void setDefaultSpawnPos(BlockPos blockPos, float f) {

        ChunkPos chunkPos = new ChunkPos(new BlockPos(levelData.getXSpawn(), 0, levelData.getZSpawn()));
        this.levelData.setSpawn(blockPos, f);

        this.getChunkSource().removeRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
        this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);
    }

    @Override
    public BlockPos findNearestMapFeature(StructureFeature<?> structureFeature, BlockPos blockPos, int i, boolean bl) {

        return !session.getProperties().worldGenSettings().generateFeatures() ? null : this.getChunkSource().getGenerator().findNearestMapFeature(this, structureFeature, blockPos, i, bl);
    }

    @Override
    public boolean isFlat() {
        return session.getProperties().worldGenSettings().isFlatWorld();
    }

    @Override
    public long getSeed() {
        return session.getProperties().worldGenSettings().seed();
    }
}
