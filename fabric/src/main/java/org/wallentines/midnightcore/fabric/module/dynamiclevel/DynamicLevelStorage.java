package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import com.mojang.datafixers.DataFixer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public class DynamicLevelStorage extends LevelStorageSource {

    public DynamicLevelStorage(Path worldsPath, Path backupsPath, DataFixer dataFixer) {

        super(worldsPath, backupsPath, dataFixer);
    }

    @Override
    public LevelStorageAccess createAccess(@NotNull String worldName) throws IOException {
        return new DynamicLevelStorageAccess(worldName);
    }

    public static DynamicLevelStorage create(Path worldsPath, Path backupsPath) {
        return new DynamicLevelStorage(worldsPath, backupsPath, DataFixers.getDataFixer());
    }

    public class DynamicLevelStorageAccess extends LevelStorageAccess {

        public DynamicLevelStorageAccess(String string) throws IOException {
            super(string);

        }

        @Nullable
        @Override
        public LevelSummary getSummary() {
            return super.getSummary();
        }
    }


}
