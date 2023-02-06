package org.wallentines.midnightcore.api.server;

import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleManager;

import java.util.UUID;

@SuppressWarnings("unused")
public interface MServer {

    /**
     * Submits a function to be run synchronously on the server thread on the next tick
     * @param runnable The function to run
     */
    void submit(Runnable runnable);

    /**
     * Determines whether this server is a local (integrated) server or not
     * @return Whether this server is a local server
     */
    boolean isLocalServer();

    /**
     * Determines whether this server is a proxy server (Bungee, Velocity)
     * Proxy servers do not have access to some functions in this class.
     * @return Whether this server is a proxy server
     */
    boolean isProxy();

    /**
     * Executes a command as the server console
     * @param command The command to run
     * @param quiet Whether output should be logged to the console
     */
    void executeCommand(String command, boolean quiet);

    /**
     * Retrieves the player manager for this server
     * @return The player manager
     */
    PlayerManager getPlayerManager();

    /**
     * Gets the player with the given UUID
     * @param uuid The UUID of the player to lookup
     * @return The player with the given UUID
     */
    default MPlayer getPlayer(UUID uuid) {
        return getPlayerManager().getPlayer(uuid);
    }


    /**
     * Retrieves an active server module with the given class
     * @param clazz The clazz of the module to look up
     * @return A module with the given class
     * @param <T> The type of module
     */
    <T extends ServerModule> T getModule(Class<T> clazz);

    /**
     * Retrieves the server's module manager
     * @return The server's module manager
     */
    ModuleManager<MServer, ServerModule> getModuleManager();


    /**
     * Returns a reference to the global MidnightCoreAPI instance
     * @return A reference to MidnightCoreAPI
     */
    MidnightCoreAPI getMidnightCore();


    /**
     * Returns a reference to the handler list for this server's tick event, which will be fired each tick
     * @return A reference to this server's tick handler list
     */
    HandlerList<ServerEvent> tickEvent();


    /**
     * Returns a reference to the configuration file where module configuration is stored. This is usually a File called
     * modules.[json/yml] in the mod/plugin's data folder, but is in the world folder on integrated servers.
     * @return A reference to the module configuration file
     */
    FileConfig getModuleConfig();

    class ServerEvent extends Event {

        private final MServer server;

        public ServerEvent(MServer server) {
            this.server = server;
        }

        public MServer getServer() {
            return server;
        }
    }


}
