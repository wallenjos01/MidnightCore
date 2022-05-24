package org.wallentines.midnightcore.fabric.module.dimension;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightcore.api.MidnightCoreAPI;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

public class DynamicServerLevel extends ServerLevel {

    private final DynamicLevelStorageSource.DynamicLevelStorageAccess session;

    public DynamicServerLevel(MinecraftServer minecraftServer, Executor executor, DynamicLevelStorageSource.DynamicLevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> dimensionType, ChunkProgressListener chunkProgressListener, ChunkGenerator chunkGenerator, boolean bl, long l, List<CustomSpawner> list, boolean bl2) {
        super(minecraftServer, executor, levelStorageAccess, serverLevelData, resourceKey, dimensionType, chunkProgressListener, chunkGenerator, bl, l, list, bl2);

        session = levelStorageAccess;
    }

    @Nullable
    public MapItemSavedData getMapData(@NotNull String string) {
        return getDataStorage().get(MapItemSavedData::load, string);
    }

    @Override
    public void setMapData(@NotNull String string, @NotNull MapItemSavedData mapItemSavedData) {
        getDataStorage().set(string, mapItemSavedData);
    }

    @Override
    public int getFreeMapId() {
        return getDataStorage().computeIfAbsent(MapIndex::load, MapIndex::new, "idcounts").getFreeAuxValueForMap();
    }

    @Override
    public void setDefaultSpawnPos(@NotNull BlockPos blockPos, float f) {

        ChunkPos chunkPos = new ChunkPos(new BlockPos(levelData.getXSpawn(), 0, levelData.getZSpawn()));
        this.levelData.setSpawn(blockPos, f);

        this.getChunkSource().removeRegionTicket(TicketType.START, chunkPos, 11, Unit.INSTANCE);
        this.getChunkSource().addRegionTicket(TicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);
    }

    @Nullable
    @Override
    public BlockPos findNearestMapFeature(@NotNull TagKey<ConfiguredStructureFeature<?, ?>> tagKey, @NotNull BlockPos blockPos, int i, boolean bl) {

        if (!this.session.getProperties().worldGenSettings().generateFeatures()) {
            return null;
        } else {
            Optional<HolderSet.Named<ConfiguredStructureFeature<?, ?>>> optional = this.registryAccess().registryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY).getTag(tagKey);
            if (optional.isEmpty()) {
                return null;
            } else {
                Pair<?, ?> pair = this.getChunkSource().getGenerator().findNearestMapFeature(this, optional.get(), blockPos, i, bl);
                return pair != null ? (BlockPos) pair.getFirst() : null;
            }
        }
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
