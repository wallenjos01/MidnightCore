package org.wallentines.midnightcore.api.module.savepoint;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public interface Savepoint {

    void load(MPlayer pl);

    boolean save(MPlayer pl);

    Identifier getId();

    ConfigSection getExtraData();

    void deserialize(ConfigSection section);

    ConfigSection serialize();

}
