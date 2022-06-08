package org.wallentines.midnightcore.fabric.module.dimension;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.mixin.AccessorMinecraftServer;

import java.io.IOException;
import java.nio.file.Path;

public class DynamicLevelStorageSource extends LevelStorageSource {

    private static final Logger LOGGER = LogManager.getLogger("DynamicLevelStorage");
    private final Path baseDir;
    private final WorldCreator creator;

    public DynamicLevelStorageSource(Path baseDir, WorldCreator creator) {
        super(baseDir, baseDir.resolve("../" + baseDir + "_backups"), DataFixers.getDataFixer());

        this.baseDir = baseDir;
        this.creator = creator;
    }

    public DynamicLevelStorageAccess createDefault() throws IOException{

        return new DynamicLevelStorageAccess();
    }

    public WorldCreator getCreator() {
        return creator;
    }

    @Override
    public Path getBaseDir() {
        return baseDir;
    }

    public class DynamicLevelStorageAccess extends LevelStorageAccess {

        private final ResourceKey<Level> defaultDimension;
        private ChunkProgressListener listener;

        private WorldData properties;
        private RegistryAccess registryAccess;

        private DynamicLevelStorageAccess() throws IOException {
            super(creator.getFolderName());
            this.defaultDimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, creator.getWorldId());

            loadProperties();
        }

        public ResourceKey<Level> getDefaultDimension() {
            return defaultDimension;
        }

        public ChunkProgressListener getListener() {
            if(listener == null) {
                listener = ((AccessorMinecraftServer) MidnightCore.getInstance().getServer()).getProgressListenerFactory().create(11);
            }
            return listener;
        }

        private void loadProperties() {

            MinecraftServer server = MidnightCore.getInstance().getServer();

            WorldLoader.InitConfig config = new WorldLoader.InitConfig(
                    new WorldLoader.PackConfig(server.getPackRepository(), server.getWorldData().getDataPackConfig(), false),
                    Commands.CommandSelection.DEDICATED,
                    server.getFunctionCompilationLevel()
            );

            WorldStem stem;
            try {
                stem = WorldStem.load(
                    config,
                    (resourceManager, dataPackConfig) -> {
                        RegistryAccess.Writable writable = RegistryAccess.builtinCopy();
                        DynamicOps<Tag> dynamicOps = RegistryOps.createAndLoad(NbtOps.INSTANCE, writable, resourceManager);
                        WorldData worldData = getDataTag(dynamicOps, dataPackConfig, writable.allElementsLifecycle());

                        if (worldData != null) {
                            return Pair.of(worldData, writable.freeze());
                        } else {

                            WorldGenSettings base = new WorldGenSettings(creator.getSeed(), creator.generateStructures(), creator.bonusChest(), server.getWorldData().worldGenSettings().dimensions());

                            LevelSettings levelSettings = new LevelSettings(creator.getLevelName(), server.getDefaultGameType(), creator.isHardcore(), creator.getDifficulty(), false, new GameRules(), dataPackConfig);
                            WorldGenSettings settings = WorldGenSettings.replaceOverworldGenerator(writable, base, creator.getGenerator());

                            PrimaryLevelData primaryLevelData = new PrimaryLevelData(levelSettings, settings, Lifecycle.stable());
                            return Pair.of(primaryLevelData, writable.freeze());
                        }
                    }, Util.backgroundExecutor(), Runnable::run).get();

            } catch (Exception ex) {
                MidnightCoreAPI.getLogger().info("Unable to load world settings for dynamic dimension in " + baseDir.toString());
                ex.printStackTrace();
                return;
            }

            this.properties = stem.worldData();
            this.registryAccess = stem.registryAccess();

            if (creator.shouldUpgradeWorld()) {

                WorldUpgrader upgrader = new WorldUpgrader(this, DataFixers.getDataFixer(), this.properties.worldGenSettings(), true);

                while(upgrader.isFinished()) {

                    int total = upgrader.getTotalChunks();
                    if (total > 0) {
                        int chunks = upgrader.getConverted() + upgrader.getSkipped();
                        LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float) chunks / (float) total * 100.0F), chunks, total);
                    }

                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
            }
            saveDataTag(registryAccess, this.properties);

        }

        public DynamicLevelStorageSource getStorageSource() {

            return DynamicLevelStorageSource.this;
        }

        public void saveDataTag() {

            saveDataTag(registryAccess, properties);
        }

        public WorldData getProperties() {
            return properties;
        }

        @Override
        public Path getDimensionPath(@NotNull ResourceKey<Level> key) {

            Path root = getBaseDir().resolve(getLevelId());

            if(key == defaultDimension) {
                return root;
            }
            if(key == Level.OVERWORLD) {
                return root.resolve("DIM0");
            }
            if(key == Level.NETHER) {
                return root.resolve("DIM-1");
            }
            if(key == Level.END) {
                return root.resolve("DIM1");
            }

            return root.resolve("dimensions/" + key.location().getNamespace() + "/" + key.location().getPath());
        }
    }

}
