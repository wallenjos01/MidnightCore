package org.wallentines.midnightcore.fabric.module.dynamiclevel;

import net.minecraft.server.level.ServerLevel;

public interface DynamicLevelCallback {

    void onLoaded(ServerLevel level);

    void onProgress(float percent);

}
