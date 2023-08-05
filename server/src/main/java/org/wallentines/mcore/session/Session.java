package org.wallentines.mcore.session;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.WrappedPlayer;
import org.wallentines.mcore.savepoint.Savepoint;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.FileExecutor;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public abstract class Session {

    protected final UUID uuid;
    protected final Server server;
    protected final String namespace;
    protected final SessionModule module;
    protected final HashSet<WrappedPlayer> players = new HashSet<>();
    public final HandlerList<Session> shutdownEvent = new SingletonHandlerList<>();

    // This should only be on for registered Sessions. As such, this should only be set by the Session Module.
    boolean isRegistered = false;

    private boolean running = true;

    /**
     * Creates a new session with the given server and namespace
     * @param module The Session Module associated with this session
     * @param server The running server
     * @param namespace The namespace for the session, may be anything
     */
    protected Session(SessionModule module, Server server, String namespace) {
        this.uuid = UUID.randomUUID();
        this.server = server;
        this.namespace = namespace;
        this.module = module;
    }

    /**
     * Gets the unique ID of the session
     * @return The unique ID of the session
     */
    public UUID getId() {
        return uuid;
    }

    /**
     * Gets the user-defined namespace of the session. May be useful for categorizing sessions.
     * @return The namespace of the session.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * The server this Session is running on
     * @return A running Server
     */
    public Server getServer() {
        return server;
    }

    /**
     * Gets a Stream of UUIDs for Players in the Session
     * @return The UUIDs of Players in the Session
     */
    public Stream<UUID> getPlayerIds() {
        return players.stream().map(WrappedPlayer::getUUID);
    }

    /**
     * Gets a Stream of Players in the Session
     * @return The Players in the Session
     */
    public Stream<Player> getPlayers() {
        return players.stream().map(WrappedPlayer::get);
    }

    /**
     * Determines whether a given Player is in the Session
     * @param player The player to lookup
     * @return Whether the player is in the Session
     */
    public boolean contains(Player player) {
        return players.contains(player.wrap());
    }

    /**
     * Broadcasts a message to all Players in the Session
     * @param message The message to broadcast
     */
    public void broadcastMessage(Component message) {
        getPlayers().forEach(pl -> pl.sendMessage(message));
    }

    /**
     * Broadcasts a message to all Players in the Session
     * @param message The message to broadcast
     */
    public void broadcastMessage(String message) {
        broadcastMessage(Component.text(message));
    }

    /**
     * Attempts to add a Player to the Session
     * @param player The Player to add
     * @return Whether adding the player was successful.
     */
    public boolean addPlayer(Player player) {

        if(!running || contains(player) || (isRegistered && module.getPlayerSession(player) != null)) return false;

        // User code should be exception-handled
        try {
            if(!shouldAddPlayer(player)) return false;
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while adding a player to a session!", ex);
            return false;
        }

        EnumSet<SavepointModule.SaveFlag> flags = null;
        try {
            flags = getSavepointFlags();
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while obtaining session savepoint slags!", ex);
        }
        if(flags != null && !flags.isEmpty()) {
            SavepointModule spm = server.getModuleManager().getModule(SavepointModule.class);
            Savepoint sp = spm.savePlayer(player, uuid.toString(), flags);

            if(isRegistered) {
                new FileExecutor(SessionModule.getRecoveryFile(player), (file) ->
                        JSONCodec.fileCodec().saveToFile(
                                ConfigContext.INSTANCE,
                                spm.getSerializer().serialize(ConfigContext.INSTANCE, sp).getOrThrow(),
                                file,
                                StandardCharsets.UTF_8)
                ).start();
            }
        }

        players.add(player.wrap());

        // User code should be exception-handled
        try {
            onAddPlayer(player);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while adding a player to a session!", ex);
            removePlayer(player);
            return false;
        }
        return true;
    }

    /**
     * Removes a Player from the Session
     * @param player The Player to remove
     */
    public void removePlayer(Player player) {

        EnumSet<SavepointModule.SaveFlag> flags = getSavepointFlags();
        if(flags != null && !flags.isEmpty()) {
            server.getModuleManager().getModule(SavepointModule.class).loadPlayer(player, uuid.toString());

            if(isRegistered) {

                // Delete session recovery if present
                new FileExecutor(SessionModule.getRecoveryFile(player), (file) ->
                {
                    if (!file.delete()) {
                        MidnightCoreAPI.LOGGER.warn("Unable to delete session recovery file at " + file.getAbsolutePath() + "! Please delete it manually, or else the session module may attempt to restore it again!");
                    }
                }
                ).start();
            }
        }

        // User code should be exception-handled
        try {
            onRemovePlayer(player);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while removing a player from a session!", ex);
        }

        players.remove(player.wrap());

        if(players.isEmpty()) {
            shutdown();
        }
    }

    /**
     * Determines whether the Session is running
     * @return Whether the Session is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Shuts down the Session, removing all players
     */
    public void shutdown() {

        if(!running) return;

        // User code should be exception-handled
        try {
            onShutdown();
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while shutting down a session!", ex);
        }

        running = false;
        getPlayers().forEach(this::removePlayer);

        shutdownEvent.invoke(this);
    }

    /**
     * Determines whether a given Player can be added to the Session. Exceptions thrown in this function will be
     * handled.
     * @param player The player to check
     * @return Whether the player can join the session
     */
    protected abstract boolean shouldAddPlayer(Player player);

    /**
     * Gets the Savepoint Flags which should be used for saving Player state. If this returns null or an empty set,
     * player state will not be saved. Exceptions thrown in this function will be handled.
     * @return The savepoint flags to use for saving
     */
    protected abstract EnumSet<SavepointModule.SaveFlag> getSavepointFlags();

    /**
     * Called when a Player is added to the session.
     * @param player The Player who was just added
     */
    protected abstract void onAddPlayer(Player player);

    /**
     * Called when a Player is removed from the session.
     * @param player The Player who was just removed
     */
    protected abstract void onRemovePlayer(Player player);

    /**
     * Called when the session is shut down
     */
    protected abstract void onShutdown();

    /**
     * An interface for creating Sessions with the given arguments
     * @param <T> The type of Session to create
     */
    public interface Factory<T extends Session> {
        T create(SessionModule module, Server server, String namespace);
    }
}
