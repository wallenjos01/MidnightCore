package org.wallentines.midnightcore.api.module.savepoint;

import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;

@SuppressWarnings("unused")
public interface SavepointModule extends ServerModule {

    void savePlayer(MPlayer pl, Identifier id);

    void loadPlayer(MPlayer pl, Identifier id);

    void removeSavePoint(MPlayer pl, Identifier id);

    void resetPlayer(MPlayer pl);

    Savepoint createSavepoint(Identifier id);

    Serializer<Savepoint> getSerializer();

}
