package org.wallentines.midnightcore.api.module.savepoint;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;

public interface SavepointModule extends Module<MidnightCoreAPI> {

    void savePlayer(MPlayer pl, Identifier id);

    void loadPlayer(MPlayer pl, Identifier id);

    void removeSavePoint(MPlayer pl, Identifier id);

    void resetPlayer(MPlayer pl);

    Savepoint createSavepoint(Identifier id);

}
