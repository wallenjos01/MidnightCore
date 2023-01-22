package org.wallentines.midnightcore.fabric.mixin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.nbt.Tag;
import net.minecraft.util.DirectoryLock;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.level.DummyFileLock;
import org.wallentines.midnightcore.fabric.level.DynamicLevelStorage;
import org.wallentines.midnightcore.fabric.level.InjectedStorageAccess;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class MixinLevelStorageAccess implements InjectedStorageAccess {

    @Unique
    private DynamicOps<Tag> dynamicOps;

    // Create a dummy file lock that always reports as valid in order to allow multiple dynamic levels to be created from the same world folder
    @Redirect(method="<init>", at=@At(value="INVOKE", target="Lnet/minecraft/util/DirectoryLock;create(Ljava/nio/file/Path;)Lnet/minecraft/util/DirectoryLock;"))
    private DirectoryLock injected(Path filelock) throws IOException {

        LevelStorageSource.LevelStorageAccess acc = (LevelStorageSource.LevelStorageAccess) (Object) this;
        if(acc instanceof DynamicLevelStorage.DynamicLevelStorageAccess dyn && dyn.getContext().getConfig().shouldIgnoreSessionLock()) {
            return DummyFileLock.createDummyLock();
        }

        return DirectoryLock.create(filelock);
    }

    // Store the original DynamicOps created when the server starts, so additional worlds can be loaded later
    @Inject(method="getDataTag", at=@At("HEAD"))
    private void injectLoadData(DynamicOps<Tag> dynamicOps, WorldDataConfiguration worldDataConfiguration, Registry<LevelStem> registry, Lifecycle lifecycle, CallbackInfoReturnable<Pair<WorldData, WorldDimensions.Complete>> cir) {

        LevelStorageSource.LevelStorageAccess acc = (LevelStorageSource.LevelStorageAccess) (Object) this;
        if(acc instanceof DynamicLevelStorage.DynamicLevelStorageAccess) return;

        this.dynamicOps = dynamicOps;
    }

    @Override
    public DynamicOps<Tag> getOps() {
        return dynamicOps;
    }
}
