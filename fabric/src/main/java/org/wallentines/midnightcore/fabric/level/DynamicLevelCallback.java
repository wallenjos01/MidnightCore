package org.wallentines.midnightcore.fabric.level;

import net.minecraft.server.level.ServerLevel;

public interface DynamicLevelCallback {

    void onLoaded(ServerLevel level);

    void onProgress(float percent);

    default void onFail() { }

}
