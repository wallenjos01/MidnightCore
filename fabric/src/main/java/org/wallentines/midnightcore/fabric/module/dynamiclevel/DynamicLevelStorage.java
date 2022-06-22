package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import com.mojang.datafixers.DataFixer;
import net.minecraft.Util;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightcore.fabric.MidnightCore;
import org.wallentines.midnightcore.fabric.mixin.AccessorLevelStorageAccess;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Consumer;

public class DynamicLevelStorage extends LevelStorageSource {

    private final HashMap<String, DynamicLevelContext> preConfigCache = new HashMap<>();
    private final DynamicLevelModule module;

    private DynamicLevelStorage(DynamicLevelModule module, Path worldsPath, Path backupsPath, DataFixer dataFixer) {
        super(worldsPath, backupsPath, dataFixer);
        this.module = module;
    }

    public DynamicLevelContext createWorldContext(String levelName, WorldConfig config) {
        return new DynamicLevelContext(MidnightCore.getInstance().getServer(), module, levelName, config, this);
    }

    @Deprecated
    @Override
    public DynamicLevelStorageAccess createAccess(@NotNull String worldId) {
        throw new UnsupportedOperationException("A DynamicLevelContext must be supplied in order to create a Dynamic world!");
    }

    DynamicLevelStorageAccess createAccess(@NotNull String worldId, DynamicLevelContext ctx) throws IOException {
        preConfigCache.put(worldId, ctx);
        return new DynamicLevelStorageAccess(worldId);
    }


    static DynamicLevelStorage create(DynamicLevelModule module, Path worldsPath, Path backupsPath) {
        return new DynamicLevelStorage(module, worldsPath, backupsPath, DataFixers.getDataFixer());
    }


    public class DynamicLevelStorageAccess extends LevelStorageAccess {

        private final DynamicLevelContext context;

        public DynamicLevelStorageAccess(String worldId) throws IOException {
            super(worldId);
            this.context = preConfigCache.remove(worldId);
        }

        public DynamicLevelContext getContext() {

            return context == null ? preConfigCache.get(getLevelId()) : context;
        }

        @Override
        public Path getDimensionPath(ResourceKey<Level> resourceKey) {

            Path root = ((AccessorLevelStorageAccess) this).getLevelDirectory().path();

            if(resourceKey.equals(context.getConfig().getRootDimensionId())) {
                return root;
            }
            if(resourceKey == Level.OVERWORLD) {
                return root.resolve("DIM0");
            }
            if(resourceKey == Level.NETHER) {
                return root.resolve("DIM-1");
            }
            if(resourceKey == Level.END) {
                return root.resolve("DIM1");
            }

            return root.resolve("dimensions").resolve(resourceKey.location().getNamespace()).resolve(resourceKey.location().getPath());
        }
    }


}
