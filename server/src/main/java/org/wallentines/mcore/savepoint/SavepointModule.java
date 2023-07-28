package org.wallentines.mcore.savepoint;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

public abstract class SavepointModule implements ServerModule {

    private final HashMap<UUID, HashMap<String, Savepoint>> savepoints = new HashMap<>();

    /**
     * Creates a Savepoint for the given player and associates it with the given name
     * @param player The player to save
     * @param name The name of the created savepoint
     * @param flags The flags defining which things to save
     * @return The created Savepoint
     */
    public Savepoint savePlayer(Player player, String name, EnumSet<SaveFlag> flags) {
        Savepoint out = createSavepoint(player, flags);
        savepoints.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(name, out);
        return out;
    }

    /**
     * Restores a player from the given named Savepoint
     * @param player The player to restore
     * @param name The name of the Savepoint to restore from
     */
    public void loadPlayer(Player player, String name) {
        HashMap<String, Savepoint> sps = savepoints.get(player.getUUID());
        if(sps == null) return;

        Savepoint sp = sps.get(name);
        if(sp == null) return;

        sp.load(player);
    }

    /**
     * Finds a player's savepoint with the given name
     * @param player The player to look up Savepoints for
     * @param name The name to lookup
     * @return The savepoint for the given player with the given name, or null
     */
    public Savepoint getSavepoint(Player player, String name) {

        HashMap<String, Savepoint> sps = savepoints.get(player.getUUID());
        if(sps == null) return null;

        return sps.get(name);
    }

    /**
     * Clears all Savepoints for the given player
     * @param player The player to clear Savepoints for
     */
    public void clearSavepoints(Player player) {
        savepoints.remove(player.getUUID());
    }

    /**
     * Removes the Savepoint with the given name for the given player
     * @param player The player to remove a Savepoint for
     * @param name The name of the Savepoint
     */
    public void removeSavepoint(Player player, String name) {

        HashMap<String, Savepoint> sps = savepoints.get(player.getUUID());
        if(sps == null) return;

        sps.remove(name);
    }

    /**
     * Resets the given player according to the given flags
     * @param player The player to reset
     * @param flags The fields to reset
     */
    public abstract void resetPlayer(Player player, EnumSet<SaveFlag> flags);

    /**
     * Gets the Savepoint factory
     * @return The Savepoint factory
     */
    public abstract Savepoint.Factory getFactory();

    /**
     * Gets the Savepoint serializer
     * @return The Savepoint serializer
     */
    public abstract Serializer<Savepoint> getSerializer();

    protected abstract Savepoint createSavepoint(Player player, EnumSet<SaveFlag> flags);


    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "savepoint");

    public enum SaveFlag {
        LOCATION,
        GAME_MODE,
        NBT,
        ADVANCEMENTS
    }
}
