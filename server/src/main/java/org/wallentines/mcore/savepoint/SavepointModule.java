package org.wallentines.mcore.savepoint;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.EnumSet;
import java.util.HashMap;

public class SavepointModule implements ServerModule {

    private final HashMap<Player, HashMap<String, Savepoint>> savepoints = new HashMap<>();
    private final Savepoint.Factory factory;

    public SavepointModule(Savepoint.Factory factory) {
        this.factory = factory;
    }

    public Savepoint savePlayer(Player player, String name, EnumSet<SaveFlag> flags) {
        Savepoint out = createSavepoint(player, flags);
        savepoints.computeIfAbsent(player, k -> new HashMap<>()).put(name, out);
        return out;
    }

    public void loadPlayer(Player player, String name) {
        HashMap<String, Savepoint> sps = savepoints.get(player);
        if(sps == null) return;

        Savepoint sp = sps.get(name);
        if(sp == null) return;

        sp.load(player);
    }

    public Savepoint getSavepoint(Player player, String name) {

        HashMap<String, Savepoint> sps = savepoints.get(player);
        if(sps == null) return null;

        return sps.get(name);
    }

    public void clearSavepoints(Player player) {
        savepoints.remove(player);
    }

    public void removeSavepoint(Player player, String name) {

        HashMap<String, Savepoint> sps = savepoints.get(player);
        if(sps == null) return;

        sps.remove(name);
    }


    protected Savepoint createSavepoint(Player player, EnumSet<SaveFlag> flags) {
        return factory.create(player, flags);
    }

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        if(factory == null) {
            MidnightCoreAPI.LOGGER.warn("Unable to initialize Savepoint Module! Invalid Factory!");
            return false;
        }

        return true;
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "savepoint");

    public enum SaveFlag {
        LOCATION,
        GAME_MODE,
        NBT,
        ADVANCEMENTS
    }
}
