package org.wallentines.mcore.savepoint;

import org.wallentines.mcore.Player;
import org.wallentines.mcore.ServerModule;

import java.util.EnumSet;
import java.util.HashMap;

public abstract class SavepointModule implements ServerModule {

    private final HashMap<Player, HashMap<String, Savepoint>> savepoints = new HashMap<>();

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


    protected abstract Savepoint createSavepoint(Player player, EnumSet<SaveFlag> flags);

    public enum SaveFlag {
        LOCATION,
        GAME_MODE,
        NBT,
        ADVANCEMENTS
    }
}
