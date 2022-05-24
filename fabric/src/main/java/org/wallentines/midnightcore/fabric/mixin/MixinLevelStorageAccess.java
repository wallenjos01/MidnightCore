package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.util.DirectoryLock;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wallentines.midnightcore.fabric.module.dimension.DummyFileLock;
import org.wallentines.midnightcore.fabric.module.dimension.DynamicLevelStorageSource;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(LevelStorageSource.LevelStorageAccess.class)
public class MixinLevelStorageAccess {

    @Redirect(method="<init>", at=@At(value="INVOKE", target="Lnet/minecraft/util/DirectoryLock;create(Ljava/nio/file/Path;)Lnet/minecraft/util/DirectoryLock;"))
    private DirectoryLock injected(Path filelock) throws IOException {

        LevelStorageSource.LevelStorageAccess acc = (LevelStorageSource.LevelStorageAccess) (Object) this;

        if(acc instanceof DynamicLevelStorageSource.DynamicLevelStorageAccess dyn && dyn.getStorageSource().getCreator().shouldIgnoreLock()) {

            return DummyFileLock.createDummyLock();
        }

        return DirectoryLock.create(filelock);
    }

}
