package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

public class DynamicLevel extends ServerLevel {

    private final HashMap<PortalType, ResourceKey<Level>> portalDestinations = new HashMap<>();
    private final LevelStorageSource.LevelStorageAccess session;

    public DynamicLevel(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List<CustomSpawner> list, boolean bl2) {
        super(minecraftServer, executor, levelStorageAccess, serverLevelData, resourceKey, levelStem, chunkProgressListener, bl, l, list, bl2);
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
    public BlockPos findNearestMapStructure(@NotNull TagKey<Structure> tagKey, @NotNull BlockPos blockPos, int i, boolean bl) {

/*        if (!this.session.getProperties().worldGenSettings().generateStructures()) {
            return null;
        } else {
            Optional<HolderSet.Named<Structure>> optional = this.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).getTag(tagKey);
            if (optional.isEmpty()) {
                return null;
            } else {
                Pair<?, ?> pair = this.getChunkSource().getGenerator().findNearestMapStructure(this, optional.get(), blockPos, i, bl);
                return pair != null ? (BlockPos) pair.getFirst() : null;
            }
        }*/
        return null;
    }

    @Override
    public boolean isFlat() {
        return false;
        //return session.getProperties().worldGenSettings().isFlatWorld();
    }

    @Override
    public long getSeed() {

        return getServer().getWorldData().worldGenSettings().seed();

        /*if(session == null) return getServer().getWorldData().worldGenSettings().seed();
        return session.getProperties().worldGenSettings().seed();*/
    }

}
