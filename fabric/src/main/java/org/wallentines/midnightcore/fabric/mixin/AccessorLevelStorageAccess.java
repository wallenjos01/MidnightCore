package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public interface AccessorLevelStorageAccess {
    @Accessor
    LevelStorageSource.LevelDirectory getLevelDirectory();
}
