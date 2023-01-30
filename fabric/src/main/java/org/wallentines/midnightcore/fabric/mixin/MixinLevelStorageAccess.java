package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.util.DirectoryLock;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.midnightcore.fabric.level.DummyFileLock;
import org.wallentines.midnightcore.fabric.level.DynamicLevelStorage;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class MixinLevelStorageAccess {

    // Create a dummy file lock that always reports as valid in order to allow multiple dynamic levels to be created from the same world folder
    @Redirect(method="<init>", at=@At(value="INVOKE", target="Lnet/minecraft/util/DirectoryLock;create(Ljava/nio/file/Path;)Lnet/minecraft/util/DirectoryLock;"))
    private DirectoryLock injected(Path filelock) throws IOException {

        LevelStorageSource.LevelStorageAccess acc = (LevelStorageSource.LevelStorageAccess) (Object) this;
        if(acc instanceof DynamicLevelStorage.DynamicLevelStorageAccess dyn && dyn.getContext().getConfig().shouldIgnoreSessionLock()) {
            return DummyFileLock.createDummyLock();
        }

        return DirectoryLock.create(filelock);
    }
}
