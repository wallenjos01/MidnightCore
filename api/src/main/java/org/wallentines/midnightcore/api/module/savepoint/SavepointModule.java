package org.wallentines.midnightcore.api.module.savepoint;

import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.EnumSet;

@SuppressWarnings("unused")
public interface SavepointModule extends ServerModule {

    default void savePlayer(MPlayer pl, Identifier id) {
        savePlayer(pl, id, EnumSet.allOf(Savepoint.SaveFlag.class));
    }

    void savePlayer(MPlayer pl, Identifier id, EnumSet<Savepoint.SaveFlag> flags);

    void loadPlayer(MPlayer pl, Identifier id);

    void removeSavePoint(MPlayer pl, Identifier id);

    void resetPlayer(MPlayer pl);

    default Savepoint createSavepoint(Identifier id) {
        return createSavepoint(id, EnumSet.allOf(Savepoint.SaveFlag.class));
    }

    Savepoint createSavepoint(Identifier id, EnumSet<Savepoint.SaveFlag> flags);

    Serializer<Savepoint> getSerializer();

}
