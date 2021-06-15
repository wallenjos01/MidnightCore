package me.m1dnightninja.midnightcore.fabric.dimension;

import com.mojang.serialization.Lifecycle;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.api.WorldCreator;
import me.m1dnightninja.midnightcore.fabric.mixin.AccessorMinecraftServer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class DynamicLevelStorageSource extends LevelStorageSource {

    private final Path baseDir;

    public DynamicLevelStorageSource(Path baseDir) {
        super(baseDir, baseDir.resolve("../" + baseDir + "_backups"), DataFixers.getDataFixer());

        this.baseDir = baseDir;
    }

    public DynamicLevelStorageAccess createDefault(String directoryName, ResourceKey<Level> defaultDimension) throws IOException {
        return new DynamicLevelStorageAccess(directoryName, defaultDimension);
    }

    public DynamicLevelStorageAccess createDefault(String directoryName) throws IOException {
        return new DynamicLevelStorageAccess(directoryName, Level.OVERWORLD);
    }

    @Override
    public Path getBaseDir() {
        return baseDir;
    }

    public class DynamicLevelStorageAccess extends LevelStorageAccess {

        private final ResourceKey<Level> defaultDimension;
        private ChunkProgressListener listener;

        private WorldData properties;

        public DynamicLevelStorageAccess(String directoryName, ResourceKey<Level> defaultDimension) throws IOException {
            super(directoryName);
            this.defaultDimension = defaultDimension;
        }

        public ResourceKey<Level> getDefaultDimension() {
            return defaultDimension;
        }

        public ChunkProgressListener getListener() {
            if(listener == null) {
                listener = ((AccessorMinecraftServer) MidnightCore.getServer()).getListenerFactory().create(11);
            }
            return listener;
        }

        public void loadProperties(WorldCreator cre) {

            RegistryAccess.RegistryHolder registryManager = (RegistryAccess.RegistryHolder) MidnightCore.getServer().registryAccess();

            RegistryReadOps<Tag> readingOps = RegistryReadOps.create(NbtOps.INSTANCE, ((AccessorMinecraftServer) MidnightCore.getServer()).getResources().getResourceManager(), registryManager);

            WorldData levelProperties = getDataTag(readingOps, ((AccessorMinecraftServer) MidnightCore.getServer()).getStorageAccess().getDataPacks());

            if(levelProperties == null) {
                MidnightCoreAPI.getLogger().warn("Unable to load " + getBaseDir() + "/level.dat! Creating default...");

                WorldGenSettings generatorOptions;
                Properties properties = new Properties();

                ChunkGenerator gen = cre.getGenerator();
                String id = gen == null ? "" : gen.toString();


                properties.put("generator-settings", id);
                properties.put("level-seed", Objects.toString(cre.getSeed()));
                properties.put("generate-structures", "true");
                properties.put("level-type", "DEFAULT");

                generatorOptions = WorldGenSettings.create(registryManager, properties);

                levelProperties = new PrimaryLevelData(cre.getLevelInfo(), generatorOptions, Lifecycle.stable());
                saveDataTag(registryManager, levelProperties);
            }

            this.properties = levelProperties;
        }

        public WorldData getProperties() {
            return properties;
        }

        @Override
        public File getDimensionPath(ResourceKey<Level> key) {

            File root = getBaseDir().resolve(getLevelId()).toFile();

            if(key == defaultDimension) {
                return root;
            }
            if(key == Level.OVERWORLD) {
                return new File("DIM0");
            }
            if(key == Level.NETHER) {
                return new File("DIM-1");
            }
            if(key == Level.END) {
                return new File("DIM1");
            }

            return new File(root, "dimensions/" + key.location().getNamespace() + "/" + key.location().getPath());
        }
    }
}
