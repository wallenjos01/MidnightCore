package org.wallentines.midnightcore.fabric.module.dimension;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.mixin.AccessorChunkMap;
import org.wallentines.midnightcore.fabric.mixin.AccessorMinecraftServer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DimensionModule implements Module<MidnightCoreAPI> {

    private static final Logger LOGGER = LogManager.getLogger("DimensionModule");

    private final HashMap<ResourceLocation, LevelStorageSource.LevelStorageAccess> loadedWorlds = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection sec, MidnightCoreAPI api) {

        return true;
    }

    private DynamicLevelStorageSource.DynamicLevelStorageAccess createLevelSession(Path location, WorldCreator creator) {

        File folder = (location == null ? Paths.get("") : location).toFile();

        DynamicLevelStorageSource sto = new DynamicLevelStorageSource(folder.toPath(), creator);
        DynamicLevelStorageSource.DynamicLevelStorageAccess sess;

        try {
            sess = sto.createDefault();

        } catch(IOException ex) {
            LOGGER.warn("An exception occurred while creating dynamic world session!");
            ex.printStackTrace();
            return null;
        }

        return sess;
    }


    public List<ResourceLocation> getWorldIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        for(ServerLevel w : MidnightCore.getInstance().getServer().getAllLevels()) {
            ids.add(w.dimension().location());
        }
        return ids;
    }

    public List<ResourceLocation> getDynamicWorldIds() {
        return new ArrayList<>(loadedWorlds.keySet());
    }

    public void createWorld(WorldCreator cre, Path location, DimensionLoadCallback cb) {

        DynamicLevelStorageSource.DynamicLevelStorageAccess session = createLevelSession(location, cre);

        if(session == null) {
            cb.onLoaded(null);
            return;
        }

        loadDimension(cre, session, cb);

    }

    public void loadDimension(WorldCreator cre, LevelStorageSource.LevelStorageAccess session, DimensionLoadCallback cb) {

        ResourceKey<Level> key = ResourceKey.create(Registry.DIMENSION_REGISTRY, cre.getWorldId());

        Map<ResourceKey<Level>, ServerLevel> worlds = ((AccessorMinecraftServer) MidnightCore.getInstance().getServer()).getLevels();
        if (worlds.containsKey(key)) {
            cb.onLoaded(worlds.get(key));
            return;
        }

        RegistryAccess.Frozen registryManager = MidnightCore.getInstance().getServer().registryAccess();

        ServerLevelData data;
        ChunkProgressListener listener;

        LevelStem stem;

        ServerLevel parent = null;

        boolean tickTime = false;
        if (session instanceof DynamicLevelStorageSource.DynamicLevelStorageAccess dSess) {

            data = (ServerLevelData) dSess.getProperties();
            listener = dSess.getListener();

            stem = ((WorldData) data).worldGenSettings().dimensions().get(cre.getDimension());

            if(stem == null) {
                stem = MidnightCore.getInstance().getServer().getWorldData().worldGenSettings().dimensions().get(cre.getDimension());
            }

            tickTime = key.equals(dSess.getDefaultDimension());
            if (!tickTime) parent = worlds.get(dSess.getDefaultDimension());

        } else {

            WorldData save = MidnightCore.getInstance().getServer().getWorldData();
            data = save.overworldData();
            listener = ((AccessorChunkMap) MidnightCore.getInstance().getServer().overworld().getChunkSource().chunkMap).getProgressListener();

            stem = save.worldGenSettings().dimensions().get(cre.getDimension());

            parent = MidnightCore.getInstance().getServer().overworld();
        }

        if(stem == null) stem = registryManager.registryOrThrow(Registry.LEVEL_STEM_REGISTRY).getOrThrow(LevelStem.OVERWORLD);

        ChunkGenerator generator = cre.getGenerator();
        if (generator != null)
            stem = new LevelStem(stem.typeHolder(), generator);


        List<CustomSpawner> spawners = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(data));
        long seedSha = BiomeManager.obfuscateSeed(cre.getSeed());

        ServerLevel world;
        if(session instanceof DynamicLevelStorageSource.DynamicLevelStorageAccess) {
            world = new DynamicServerLevel(MidnightCore.getInstance().getServer(), Util.backgroundExecutor(), (DynamicLevelStorageSource.DynamicLevelStorageAccess) session, data, key, stem, listener, false, seedSha, spawners, tickTime);
        } else {
            world = new ServerLevel(MidnightCore.getInstance().getServer(), Util.backgroundExecutor(), session, data, key, stem, listener, false, seedSha, spawners, tickTime);
        }

        if (parent == null) {
            world.getWorldBorder().applySettings(data.getWorldBorder());
        } else {
            parent.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(world.getWorldBorder()));
        }

        loadedWorlds.put(cre.getWorldId(), session);

        if (!data.isInitialized()) {
            if (cre.getSpawnPosition() == null) {
                AccessorMinecraftServer.callSetInitialSpawn(world, data, false, false);
            } else {
                data.setSpawn(cre.getSpawnPosition(), 0.0f);
            }
            data.setInitialized(true);
        }

        Thread worker = new Thread(() -> {

            world.setDefaultSpawnPos(new BlockPos(data.getXSpawn(), data.getYSpawn(), data.getZSpawn()), data.getSpawnAngle());
            listener.updateSpawnPos(new ChunkPos(world.getSharedSpawnPos()));

            ServerChunkCache prov = world.getChunkSource();

            ChunkPos pos = new ChunkPos(world.getSharedSpawnPos());
            prov.getLightEngine().setTaskPerBatch(500);
            prov.addRegionTicket(TicketType.START, pos, 11, Unit.INSTANCE);

            MidnightCore.getInstance().getServer().submit(() -> {

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
            LOGGER.warn("You cannot unload the overworld!");
            return false;
        }

        ResourceKey<Level> key = ResourceKey.create(Registry.DIMENSION_REGISTRY, id);
        ServerLevel world = MidnightCore.getInstance().getServer().getLevel(key);
        if(world == null) {
            LOGGER.warn("A request was made to unload a dimension that does not exist!");
            return false;
        }

        LevelStorageSource.LevelStorageAccess defaultSession = ((AccessorMinecraftServer) MidnightCore.getInstance().getServer()).getStorageSource();
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
                    if(save && session instanceof DynamicLevelStorageSource.DynamicLevelStorageAccess acc) {
                        acc.saveDataTag();
                    }

                    session.close();
                }
            }

        } catch (IOException ex) {
            LOGGER.warn("An exception occurred while unloading a world!");
            ex.printStackTrace();
            return false;
        }

        loadedWorlds.remove(id);
        ((AccessorMinecraftServer) MidnightCore.getInstance().getServer()).getLevels().remove(key);

        return true;
    }

    public interface DimensionLoadCallback {

        void onLoaded(ServerLevel world);

    }

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "dimension");
    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(DimensionModule::new, ID, new ConfigSection());
}
