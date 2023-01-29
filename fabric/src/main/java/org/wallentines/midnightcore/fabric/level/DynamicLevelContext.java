package org.wallentines.midnightcore.fabric.level;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapIndex;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightcore.fabric.mixin.AccessorMinecraftServer;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class DynamicLevelContext {

    private final MinecraftServer server;
    private final WorldConfig config;
    private final DynamicLevelStorage.DynamicLevelStorageAccess storageAccess;
    private final WorldData worldData;
    private final ChunkProgressListener chunkProgressListener;
    private final Map<ResourceKey<Level>, DynamicLevel> levels = new HashMap<>();
    private boolean removed = false;

    public DynamicLevelContext(MinecraftServer server, String levelName, WorldConfig config, DynamicLevelStorage storage) {

        this.server = server;
        this.config = config;

        Event.register(ServerStopEvent.class, this, ev -> {
            if(!isRemoved()) unload(!config.shouldNotSave());
        });

        try {
            this.storageAccess = storage.createAccess(levelName, this);

        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create dynamic level!");
        }

        this.chunkProgressListener = ((AccessorMinecraftServer) server).getProgressListenerFactory().create(11);

        try {

            WorldDataConfiguration worldDataConfiguration = server.getWorldData().getDataConfiguration();

            WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(server.getPackRepository(), worldDataConfiguration, false, false);
            WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(
                packConfig,
                server.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED,
                server.getFunctionCompilationLevel()
            );

            WorldStem stem = Util.blockUntilDone(executor -> WorldLoader.load(initConfig, dataLoadContext -> {

                RegistryAccess.Frozen access = server.registryAccess();
                Registry<LevelStem> registry = access.registryOrThrow(Registries.LEVEL_STEM); // Load dimension types from global datapacks only

                DynamicOps<Tag> ops = ((InjectedStorageAccess) ((AccessorMinecraftServer) server).getStorageSource()).getOps();
                Pair<WorldData, WorldDimensions.Complete> data = storageAccess.getDataTag(ops, dataLoadContext.dataConfiguration(), registry, Lifecycle.stable());

                if (data != null) {
                    return new WorldLoader.DataLoadOutput<>(data.getFirst(), data.getSecond().dimensionsRegistryAccess());
                }

                Registry<WorldPreset> presetRegistry = access.registryOrThrow(Registries.WORLD_PRESET);
                Holder.Reference<WorldPreset> defaultWorldPreset = presetRegistry.getHolder(WorldPresets.NORMAL)
                        .or(() -> presetRegistry.holders().findAny())
                        .orElseThrow(() -> new IllegalStateException("Invalid datapack contents: can't find default world preset for dynamic level " + levelName + "!"));

                Holder.Reference<WorldPreset> worldPreset = presetRegistry.getHolder(config.getWorldType()).orElseGet(() -> {
                    MidnightCoreAPI.getLogger().warn("Failed to find level type for dynamic level " + levelName + ", defaulting to " + defaultWorldPreset.unwrapKey().map(key -> key.location().toString()).orElse("[unnamed]"));
                    return defaultWorldPreset;
                });

                LevelSettings levelSettings = new LevelSettings(
                        levelName,
                        config.getDefaultGameMode(),
                        config.isHardcore(),
                        config.getDifficulty(),
                        false, new GameRules(), dataLoadContext.dataConfiguration());

                WorldOptions worldOptions = new WorldOptions(config.getSeed(), config.shouldGenerateStructures(), config.hasBonusChest());
                WorldDimensions worldDimensions = new WorldDimensions(registry);

                if(config.getGenerator() != null) {

                    //worldDimensions = replaceGenerator(worldDimensions, config.getRootDimensionType(), config.getGenerator());
                    worldDimensions = replaceGenerator(registry, config.getRootDimensionType(), config.getGenerator());

                }
                else if (worldPreset.is(WorldPresets.FLAT)) {

                    RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, access);

                    JsonObject generatorSettings = JsonConfigProvider.INSTANCE.toJson(config.getGeneratorSettings());
                    DataResult<FlatLevelGeneratorSettings> result = FlatLevelGeneratorSettings.CODEC.parse(new Dynamic<>(registryOps, generatorSettings));

                    Optional<FlatLevelGeneratorSettings> settings = result.resultOrPartial(str -> MidnightCoreAPI.getLogger().error(str));
                    if (settings.isPresent()) {
                        worldDimensions = replaceGenerator(
                                registry,
                                config.getRootDimensionType(),
                                new FlatLevelSource(settings.get())
                        );
                    }
                }

                WorldDimensions.Complete complete = worldDimensions.bake(worldDimensions.dimensions());
                Lifecycle lifecycle = complete.lifecycle().add(dataLoadContext.datapackWorldgen().allRegistriesLifecycle());

                PrimaryLevelData primary = new PrimaryLevelData(levelSettings, worldOptions, complete.specialWorldProperty(), lifecycle);
                //this.storageAccess.saveDataTag(access, primary);

                return new WorldLoader.DataLoadOutput<>(primary, complete.dimensionsRegistryAccess());

            }, WorldStem::new, Util.backgroundExecutor(), executor)).get();

            this.worldData = stem.worldData();

        } catch (Exception ex) {

            ex.printStackTrace();
            throw new IllegalStateException("Failed to load datapacks for dynamic dimension " + storageAccess.getLevelId() + "! World load aborted.");
        }

    }

    public WorldConfig getConfig() {
        return config;
    }

    public DynamicLevelStorage.DynamicLevelStorageAccess getStorageAccess() {
        return storageAccess;
    }

    public Map<ResourceKey<Level>, DynamicLevel> getLevels() {
        return levels;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void loadDimension(ResourceKey<Level> dimensionKey, Consumer<ServerLevel> onComplete) {

        loadDimension(dimensionKey, new DynamicLevelCallback() {
            @Override
            public void onLoaded(ServerLevel level) {
                onComplete.accept(level);
            }

            @Override
            public void onProgress(float percent) { }
        });
    }
    public void loadDimension(ResourceKey<Level> dimensionKey, DynamicLevelCallback callback) {

        if (removed) throw new IllegalStateException("Attempt to access an unloaded DynamicLevelContext!");

        if (levels.containsKey(dimensionKey)) {
            callback.onLoaded(levels.get(dimensionKey));
            return;
        }

        chunkProgressListener.start();

        boolean root = dimensionKey.equals(config.getRootDimensionId());
        ServerLevelData serverLevelData = root ? worldData.overworldData() : new DerivedLevelData(worldData, worldData.overworldData());

        boolean debug = worldData.isDebugWorld();
        long seed = worldData.worldGenOptions().seed();
        long seedHash = BiomeManager.obfuscateSeed(seed);

        List<CustomSpawner> spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverLevelData));

        //Registry<LevelStem> levelStemRegistry = levelSettings.dimensions();
        Registry<LevelStem> levelStemRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
        LevelStem rootLevel = levelStemRegistry.getOrThrow(config.getRootDimensionType());

        DynamicLevel level = new DynamicLevel(Util.backgroundExecutor(), serverLevelData, dimensionKey, rootLevel, chunkProgressListener, seedHash, spawners, root);
        levels.put(dimensionKey, level);

        ((AccessorMinecraftServer) server).getLevels().put(dimensionKey, level);

        ExecutorService exe = Util.backgroundExecutor();
        exe.submit(() -> {

            try {
                ServerChunkCache serverChunkCache = level.getChunkSource();
                serverChunkCache.getLightEngine().setTaskPerBatch(500);

                BlockPos spawn = new BlockPos(serverLevelData.getXSpawn(), serverLevelData.getYSpawn(), serverLevelData.getZSpawn());
                level.setDefaultSpawnPos(spawn, serverLevelData.getSpawnAngle());
                chunkProgressListener.updateSpawnPos(new ChunkPos(spawn));

                while (serverChunkCache.getTickingGenerated() < 441) {
                    Thread.yield();
                    LockSupport.parkNanos("waiting for tasks", 100000L);

                    float progress = Math.min(1.0f, (float) serverChunkCache.getTickingGenerated() / 441.0f);
                    try {
                        callback.onProgress(progress);
                    } catch (Exception ex) {
                        MidnightCoreAPI.getLogger().warn("An exception occurred while sending a dynamic dimension load progress callback!");
                        ex.printStackTrace();
                    }
                }

                if (root) {
                    if (!serverLevelData.isInitialized()) {
                        AccessorMinecraftServer.callSetInitialSpawn(level, serverLevelData, config.hasBonusChest(), debug);
                        if (debug) {
                            ((AccessorMinecraftServer) server).callSetupDebugLevel(worldData);
                        }
                        serverLevelData.setInitialized(true);
                    }
                    addWorldBorderListener(level);
                } else {
                    WorldBorder wb = levels.get(config.getRootDimensionId()).getWorldBorder();
                    wb.addListener(new BorderChangeListener.DelegateBorderChangeListener(level.getWorldBorder()));
                }

                // Load Chunks
                ForcedChunksSavedData chunksSavedData = level.getDataStorage().get(ForcedChunksSavedData::load, ForcedChunksSavedData.FILE_ID);

                if (chunksSavedData != null) {
                    for (long l : chunksSavedData.getChunks()) {
                        ChunkPos pos = new ChunkPos(l);
                        serverChunkCache.updateChunkForced(pos, true);
                    }
                }

                serverChunkCache.getLightEngine().setTaskPerBatch(5);
                level.setSpawnSettings(server.isSpawningMonsters(), server.isSpawningAnimals());

                chunkProgressListener.stop();
                callback.onLoaded(level);
            } catch (Exception ex) {
                MidnightCoreAPI.getLogger().warn("An exception occurred while loading a dynamic dimension!");
                ex.printStackTrace();
            }
        });
    }

    public static void addWorldBorderListener(ServerLevel serverLevel) {

        serverLevel.getWorldBorder().addListener(new BorderChangeListener() {
            @Override
            public void onBorderSizeSet(@NotNull WorldBorder worldBorder, double d) {
                ClientboundSetBorderSizePacket pck = new ClientboundSetBorderSizePacket(worldBorder);
                serverLevel.players().forEach(pl -> pl.connection.send(pck));
            }

            @Override
            public void onBorderSizeLerping(@NotNull WorldBorder worldBorder, double d, double e, long l) {
                ClientboundSetBorderLerpSizePacket pck = new ClientboundSetBorderLerpSizePacket(worldBorder);
                serverLevel.players().forEach(pl -> pl.connection.send(pck));
            }

            @Override
            public void onBorderCenterSet(@NotNull WorldBorder worldBorder, double d, double e) {
                ClientboundSetBorderCenterPacket pck = new ClientboundSetBorderCenterPacket(worldBorder);
                serverLevel.players().forEach(pl -> pl.connection.send(pck));
            }

            @Override
            public void onBorderSetWarningTime(@NotNull WorldBorder worldBorder, int i) {
                ClientboundSetBorderWarningDelayPacket pck = new ClientboundSetBorderWarningDelayPacket(worldBorder);
                serverLevel.players().forEach(pl -> pl.connection.send(pck));
            }

            @Override
            public void onBorderSetWarningBlocks(@NotNull WorldBorder worldBorder, int i) {
                ClientboundSetBorderWarningDistancePacket pck = new ClientboundSetBorderWarningDistancePacket(worldBorder);
                serverLevel.players().forEach(pl -> pl.connection.send(pck));
            }

            @Override
            public void onBorderSetDamagePerBlock(@NotNull WorldBorder worldBorder, double d) { }

            @Override
            public void onBorderSetDamageSafeZOne(@NotNull WorldBorder worldBorder, double d) { }
        });
    }

    public void unloadDimension(ResourceKey<Level> dimensionKey, boolean save) {

        ((AccessorMinecraftServer) server).getLevels().remove(dimensionKey);

        if(removed) throw new IllegalStateException("Attempt to access an unloaded DynamicLevelContext!");

        DynamicLevel level = levels.remove(dimensionKey);
        if(level == null) return;


        // Save world
        if(save) {
            level.save(null, true, true);
        }

        // Unload world
        try {
            level.getChunkSource().close();

        } catch (IOException ex) {
            MidnightCoreAPI.getLogger().warn("An exception occurred while unloading a dynamic level!");
            ex.printStackTrace();
        }
    }

    public void unload(boolean save) {

        if(removed) throw new IllegalStateException("Attempt to access an unloaded DynamicLevelContext!");

        for(ResourceKey<Level> key : new ArrayList<>(levels.keySet())) {
            unloadDimension(key, save);
        }

        try {
            // Unload session
            if (save) {
                storageAccess.saveDataTag(server.registryAccess(), worldData);
            }
            storageAccess.close();

        } catch (IOException ex) {
            MidnightCoreAPI.getLogger().warn("An exception occurred while unloading a dynamic world!");
            ex.printStackTrace();
        }

        removed = true;
    }

    private static WorldDimensions replaceGenerator(Registry<LevelStem> registry, ResourceKey<LevelStem> dimensionKey, ChunkGenerator generator) {

        LevelStem stem = registry.getOrThrow(dimensionKey);

        MappedRegistry<LevelStem> writableRegistry = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.experimental());
        writableRegistry.register(dimensionKey, new LevelStem(stem.type(), generator), Lifecycle.stable());

        for(Map.Entry<ResourceKey<LevelStem>, LevelStem> st : registry.entrySet()) {

            if(st.getKey() != dimensionKey) {
                writableRegistry.registerMapping(registry.getId(st.getValue()), st.getKey(), st.getValue(), registry.lifecycle(st.getValue()));
            }
        }

        return new WorldDimensions(writableRegistry);
    }

    @ParametersAreNonnullByDefault
    public class DynamicLevel extends ServerLevel {

        public DynamicLevel(Executor executor, ServerLevelData serverLevelData, ResourceKey<Level> dimensionKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, long seed, List<CustomSpawner> spawners, boolean tickTime) {
            super(server, executor, storageAccess, serverLevelData, dimensionKey, levelStem, chunkProgressListener, false, seed, spawners, tickTime);
        }

        public ResourceKey<Level> getRoot() {
            return config.getRootDimensionId();
        }

        public ResourceKey<Level> getNether() {
            return config.getNetherDimensionId();
        }

        public ResourceKey<Level> getEnd() {
            return config.getEndDimensionId();
        }
        @Override
        public long getSeed() {
            return config.getSeed();
        }

        @Nullable
        public MapItemSavedData getMapData(String string) {
            return getDataStorage().get(MapItemSavedData::load, string);
        }

        @Override
        public boolean noSave() {
            return config.shouldNotSave();
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

        @Nullable
        @Override
        public BlockPos findNearestMapStructure(TagKey<Structure> tagKey, BlockPos blockPos, int i, boolean bl) {

            if (!worldData.worldGenOptions().generateStructures()) {
                return null;
            } else {
                Optional<HolderSet.Named<Structure>> optional = server.registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(tagKey);
                if (optional.isEmpty()) {
                    return null;
                } else {
                    Pair<?, ?> pair = this.getChunkSource().getGenerator().findNearestMapStructure(this, optional.get(), blockPos, i, bl);
                    return pair != null ? (BlockPos) pair.getFirst() : null;
                }
            }
        }

        @Override
        public boolean isFlat() {
            return worldData.isFlatWorld();
        }
    }

}
