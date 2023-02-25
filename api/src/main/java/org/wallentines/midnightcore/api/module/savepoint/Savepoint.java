package org.wallentines.midnightcore.api.module.savepoint;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.EnumSet;

public interface Savepoint {

    void load(MPlayer pl);

    boolean save(MPlayer pl);

    EnumSet<SaveFlag> getFlags();

    Identifier getId();

    ConfigSection getExtraData();

    enum SaveFlag {

        LOCATION,
        GAME_MODE,
        DATA_TAG,
        ADVANCEMENTS
    }

}
