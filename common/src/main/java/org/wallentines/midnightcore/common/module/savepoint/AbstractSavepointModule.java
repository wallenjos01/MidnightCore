package org.wallentines.midnightcore.common.module.savepoint;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.module.savepoint.SavepointModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.EnumSet;
import java.util.HashMap;

@SuppressWarnings("unused")
public abstract class AbstractSavepointModule implements SavepointModule {

    private final HashMap<MPlayer, HashMap<Identifier, Savepoint>> savepoints = new HashMap<>();


    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        return true;
    }

    @Override
    public void savePlayer(MPlayer pl, Identifier id, EnumSet<Savepoint.SaveFlag> flags) {

        Savepoint sp = createSavepoint(id, flags);
        if(sp == null || !sp.save(pl)) return;

        savepoints.compute(pl, (k,v) -> {
            if(v == null) v = new HashMap<>();
            v.put(id, sp);
            return v;
        });
    }

    @Override
    public void loadPlayer(MPlayer pl, Identifier id) {

        if(!savepoints.containsKey(pl)) return;
        Savepoint sp = savepoints.get(pl).get(id);

        if(sp != null) sp.load(pl);
    }

    @Override
    public void removeSavePoint(MPlayer pl, Identifier id) {

        if(!savepoints.containsKey(pl)) return;
        savepoints.get(pl).remove(id);
    }

    protected static final ConfigSection DEFAULT_CONFIG = new ConfigSection();

    public static final Identifier ID = new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "savepoint");

}
