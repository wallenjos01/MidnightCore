package org.wallentines.mcore.adapter.v1_19_R1;

import net.minecraft.SharedConstants;
import org.wallentines.mcore.GameVersion;

public class VersionUtil {

    public static GameVersion getGameVersion() {
        return new GameVersion(SharedConstants.b().getId(), SharedConstants.b().getProtocolVersion());
    }
}
