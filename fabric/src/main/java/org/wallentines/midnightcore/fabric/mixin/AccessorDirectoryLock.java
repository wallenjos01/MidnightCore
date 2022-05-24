package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.util.DirectoryLock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

@Mixin(DirectoryLock.class)
public interface AccessorDirectoryLock {
    @Invoker("<init>")
    static DirectoryLock createDirectoryLock(FileChannel fileChannel, FileLock fileLock) {
        throw new UnsupportedOperationException();
    }
}
