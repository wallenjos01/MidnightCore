package org.wallentines.midnightcore.api.module.session;

import org.wallentines.midnightcore.api.module.lang.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

public interface Session {


    /**
     * Retrieves the Unique ID of the session
     *
     * @return The Session's Unique ID
     */
    UUID getId();

    /**
     * Attempts to add a player to the session
     *
     * @param player The player to add
     * @return       Whether the attempt was successful.
     */
    boolean addPlayer(MPlayer player);

    /**
     * Removes a player from the session
     *
     * @param player The player to remove
     */
    void removePlayer(MPlayer player);

    /**
     * Registers a callback to be run when the session shuts down
     *
     * @param runnable The code to be run
     */
    void addShutdownCallback(Runnable runnable);

    /**
     * Generates a collection of all the players in the session
     *
     * @return All the players in the session
     */
    Collection<MPlayer> getPlayers();

    /**
     * Retrieves a random player that is in the session
     *
     * @return A random player in the session
     */
    MPlayer getRandomPlayer();

    /**
     * Generates a collection of all the players in the session who meet a certain criteria
     *
     * @param filter A function to run on each player to determine if they should be included
     * @return       All the players in the session
     */
    Collection<MPlayer> getPlayers(Function<MPlayer, Boolean> filter);

    /**
     * Determines the number of players in the session
     *
     * @return The number of players in the session
     */
    int getPlayerCount();

    /**
     * Checks whether a particular player is in the session
     *
     * @param player The player to query
     * @return       Whether the player is in the session
     */
    boolean contains(MPlayer player);

    /**
     * Shuts down the session and removes all players
     */
    void shutdown();

    /**
     * Sends a message to each player in the session
     *
     * @param message The message to send.
     */
    void broadcastMessage(MComponent message);

    /**
     * Sends a lang message to each player in the session
     *
     * @param key       The key of the message to send.
     * @param provider  The lang provider to use
     * @param data      The data to use in the lookup.
     */
    void broadcastMessage(String key, LangProvider provider, Object... data);

    void tick();

}
