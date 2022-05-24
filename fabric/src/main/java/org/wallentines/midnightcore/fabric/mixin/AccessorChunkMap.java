package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.progress.ChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface AccessorChunkMap {

    @Accessor("progressListener")
    ChunkProgressListener getProgressListener();

}
