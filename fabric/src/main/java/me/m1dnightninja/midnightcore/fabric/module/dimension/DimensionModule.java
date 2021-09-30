package me.m1dnightninja.midnightcore.fabric.module.dimension;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.mixin.AccessorAnvilChunkStorage;
import me.m1dnightninja.midnightcore.fabric.mixin.AccessorMinecraftServer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class DimensionModule implements IModule {

    private static final MIdentifier ID = MIdentifier.create("midnightcore", "dimension");

    private final HashMap<ResourceLocation, LevelStorageSource.LevelStorageAccess> loadedWorlds = new HashMap<>();
    private final HashMap<ResourceLocation, ChunkGenerator> generators = new HashMap<>();


    @Override
    public boolean initialize(ConfigSection sec) {

        return true;
    }

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return null;
    }

    public void registerGeneratorType(ResourceLocation id, ChunkGenerator generator) {

        if(generators.containsKey(id)) {
            MidnightCoreAPI.getLogger().warn("Attempt to register a chunk generator with duplicate ID!");
            return;
        }

        generators.put(id, generator);
    }

    public ChunkGenerator getChunkGenerator(ResourceLocation id) {

        return generators.get(id);
    }

    private DynamicLevelStorageSource.DynamicLevelStorageAccess createLevelSession(Path location, String worldName, ResourceKey<Level> defaultDimension) {

        File folder = (location == null ? Paths.get("") : location).toFile();

        DynamicLevelStorageSource sto = new DynamicLevelStorageSource(folder.toPath());
        DynamicLevelStorageSource.DynamicLevelStorageAccess sess;

        try {
            sess = sto.createDefault(worldName, defaultDimension);

        } catch(IOException ex) {
            MidnightCoreAPI.getLogger().warn("An exception occurred while creating dynamic world session!");
            ex.printStackTrace();
            return null;
        }

        MinecraftServer.convertFromRegionFormatIfNeeded(sess);

        return sess;
    }


    public List<ResourceLocation> getWorldIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for(ServerLevel w : MidnightCore.getServer().getAllLevels()) {
            ids.add(w.dimension().location());
        }
        return ids;
    }

    public List<ResourceLocation> getDynamicWorldIds() {
        return new ArrayList<>(loadedWorlds.keySet());
    }

    public void createWorld(WorldCreator cre, Path location, DimensionLoadCallback cb) {

        ResourceKey<Level> key = ResourceKey.create(Registry.DIMENSION_REGISTRY, cre.getWorldId());
        DynamicLevelStorageSource.DynamicLevelStorageAccess session = createLevelSession(location, cre.getFolderName(), key);

        if(session == null) {
            cb.onLoaded(null);
            return;
        }

        session.loadProperties(cre);
        loadDimension(cre, session, cb);

    }

    public void loadDimension(WorldCreator cre, LevelStorageSource.LevelStorageAccess session, DimensionLoadCallback cb) {

        ResourceKey<Level> key = ResourceKey.create(Registry.DIMENSION_REGISTRY, cre.getWorldId());

        Map<ResourceKey<Level>, ServerLevel> worlds = ((AccessorMinecraftServer) MidnightCore.getServer()).getWorlds();
        if (worlds.containsKey(key)) {
            cb.onLoaded(worlds.get(key));
            return;
        }

        RegistryAccess.RegistryHolder registryManager = (RegistryAccess.RegistryHolder) MidnightCore.getServer().registryAccess();

        ServerLevelData props;
        ChunkProgressListener listener;
        LevelStem options;

        ServerLevel parent = null;

        boolean tickTime = false;
        if (session instanceof DynamicLevelStorageSource.DynamicLevelStorageAccess) {

            DynamicLevelStorageSource.DynamicLevelStorageAccess dSess = (DynamicLevelStorageSource.DynamicLevelStorageAccess) session;

            if (dSess.getProperties() == null) {
                dSess.loadProperties(cre);
            }

            props = (ServerLevelData) dSess.getProperties();
            listener = dSess.getListener();

            options = ((WorldData) props).worldGenSettings().dimensions().get(cre.getDimension());

            if(options == null) {
                options = MidnightCore.getServer().getWorldData().worldGenSettings().dimensions().get(cre.getDimension());
            }

            tickTime = key.equals(dSess.getDefaultDimension());
            if (!tickTime) parent = worlds.get(dSess.getDefaultDimension());

        } else {

            WorldData save = MidnightCore.getServer().getWorldData();
            props = save.overworldData();
            listener = ((AccessorAnvilChunkStorage) MidnightCore.getServer().overworld().getChunkSource().chunkMap).getListener();

            options = save.worldGenSettings().dimensions().get(cre.getDimension());

            parent = MidnightCore.getServer().overworld();
        }

        DimensionType type;
        if (options == null) {
            MidnightCoreAPI.getLogger().warn("Unable to find dimension " + cre.getDimension() + "! Defaulting to overworld!");
            type = registryManager.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(DimensionType.OVERWORLD_LOCATION);
        } else {
            type = options.type();
        }

        ChunkGenerator generator = cre.getGenerator();
        if (generator == null)
            generator = options == null ? MidnightCore.getServer().overworld().getChunkSource().getGenerator() : options.generator();

        List<CustomSpawner> spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(props));
        long seedSha = BiomeManager.obfuscateSeed(cre.getSeed());

        ServerLevel world;
        if(session instanceof DynamicLevelStorageSource.DynamicLevelStorageAccess) {
            world = new DynamicLevel(MidnightCore.getServer(), Util.backgroundExecutor(), (DynamicLevelStorageSource.DynamicLevelStorageAccess) session, props, key, type, listener, generator, false, seedSha, spawners, tickTime);
        } else {
            world = new ServerLevel(MidnightCore.getServer(), Util.backgroundExecutor(), session, props, key, type, listener, generator, false, seedSha, spawners, tickTime);
        }

        if (parent == null) {
            world.getWorldBorder().applySettings(props.getWorldBorder());
        } else {
            parent.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(world.getWorldBorder()));
        }

        loadedWorlds.put(cre.getWorldId(), session);

        if (!props.isInitialized()) {
            if (cre.getSpawnPosition() == null) {
                AccessorMinecraftServer.callSetInitialSpawn(world, props, false, false);
            } else {
                props.setSpawn(cre.getSpawnPosition(), 0.0f);
            }
            props.setInitialized(true);
        }

        Thread worker = new Thread(() -> {

            world.setDefaultSpawnPos(new BlockPos(props.getXSpawn(), props.getYSpawn(), props.getZSpawn()), props.getSpawnAngle());
            listener.updateSpawnPos(new ChunkPos(world.getSharedSpawnPos()));

            ServerChunkCache prov = world.getChunkSource();

            ChunkPos pos = new ChunkPos(world.getSharedSpawnPos());
            prov.getLightEngine().setTaskPerBatch(500);
            prov.addRegionTicket(TicketType.START, pos, 11, Unit.INSTANCE);

            MidnightCore.getServer().submit(() -> {

                worlds.put(key, world);

                ForcedChunksSavedData state = world.getDataStorage().get(ForcedChunksSavedData::load, "chunks");

                listener.stop();

                if(state != null) {

                    LongIterator longIterator = state.getChunks().iterator();

                    while(longIterator.hasNext()) {
                        long l = longIterator.nextLong();
                        ChunkPos chunk = new ChunkPos(l);
                        world.getChunkSource().updateChunkForced(chunk, true);
                    }
                }

                prov.getLightEngine().setTaskPerBatch(5);
                world.setSpawnSettings(true, true);

                cb.onLoaded(world);
            });



        });
        worker.start();

    }

    public boolean unloadDimension(ResourceLocation id, boolean save) {

        if(id.equals(Level.OVERWORLD.location())) {
            MidnightCoreAPI.getLogger().warn("You cannot unload the overworld!");
            return false;
        }

        ResourceKey<Level> key = ResourceKey.create(Registry.DIMENSION_REGISTRY, id);
        ServerLevel world = MidnightCore.getServer().getLevel(key);
        if(world == null) {
            MidnightCoreAPI.getLogger().warn("A request was made to unload a dimension that does not exist!");
            return false;
        }

        LevelStorageSource.LevelStorageAccess defaultSession = ((AccessorMinecraftServer) MidnightCore.getServer()).getStorageAccess();

        LevelStorageSource.LevelStorageAccess session = loadedWorlds.getOrDefault(id, defaultSession);

        if(save) {
            world.save(null, true, true);
        }

        try {
            world.getChunkSource().close();

            if(session != defaultSession) {

                int found = 0;
                for(LevelStorageSource.LevelStorageAccess other : loadedWorlds.values()) {
                    if(other == session) found++;
                }
                if(found == 1) {
                    if(save && session instanceof DynamicLevelStorageSource.DynamicLevelStorageAccess) {
                        session.saveDataTag(MidnightCore.getServer().registryAccess(), ((DynamicLevelStorageSource.DynamicLevelStorageAccess) session).getProperties());
                    }

                    session.close();
                }
            }

        } catch (IOException ex) {
            MidnightCoreAPI.getLogger().warn("An exception occurred while unloading a world!");
            ex.printStackTrace();
            return false;
        }

        loadedWorlds.remove(id);
        ((AccessorMinecraftServer) MidnightCore.getServer()).getWorlds().remove(key);

        return true;
    }

    public interface DimensionLoadCallback {

        void onLoaded(ServerLevel world);

    }

}
