package org.wallentines.midnightcore.common.module.savepoint;

import org.wallentines.midnightcore.api.module.savepoint.Savepoint;
import org.wallentines.midnightcore.api.module.savepoint.SavepointModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigRegistry;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.ConfigSerializer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;

@SuppressWarnings("unused")
public abstract class AbstractSavepointModule implements SavepointModule {

    private final HashMap<MPlayer, HashMap<Identifier, Savepoint>> savepoints = new HashMap<>();


    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        ConfigRegistry.INSTANCE.registerSerializer(Savepoint.class, new ConfigSerializer<>() {
            @Override
            public Savepoint deserialize(ConfigSection section) {

                Savepoint sp = createSavepoint(section.get("id", Identifier.class));
                sp.deserialize(section);

                return sp;
            }

            @Override
            public ConfigSection serialize(Savepoint object) {
                return object.serialize();
            }
        });

        return true;
    }

    @Override
    public void savePlayer(MPlayer pl, Identifier id) {

        Savepoint sp = createSavepoint(id);
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

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "savepoint");

}
