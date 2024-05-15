package org.wallentines.mcore;

import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.types.Singleton;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * An interface representing a Proxy
 */
public interface Proxy {

    /**
     * Gets the path where configuration files should be stored. (i.e. the "plugins" folder)
     * @return The configuration file directory
     */
    Path getConfigDirectory();

    /**
     * Gets the proxy's module manager
     * @return The module manager
     */
    ModuleManager<Proxy, ProxyModule> getModuleManager();

    /**
     * Gets the player with the given UUID
     * @param uuid The UUID to look up
     * @return The found player, or null
     */
    ProxyPlayer getPlayer(UUID uuid);

    /**
     * Gets a stream of all players connected to the proxy
     * @return A stream of players
     */
    Stream<ProxyPlayer> getPlayers();

    /**
     * Gets the number of players connected to the proxy
     * @return The number of online players
     */
    int getPlayerCount();

    /**
     * Gets the server with the given name
     * @param name The name to look up
     * @return The found server, or null
     */
    ProxyServer getServer(String name);

    /**
     * Loads all modules from the given registry using the proxy's module config
     * @param registry The registry to find modules in
     */
    default void loadModules(Registry<ModuleInfo<Proxy, ProxyModule>> registry) {

        ModuleUtil.loadModules(getModuleManager(), registry, this, getModuleConfig());
        shutdownEvent().register(this, ev -> getModuleManager().unloadAll());

    }

    /**
     * Gets the configuration file for modules
     * @return The module config
     */
    default FileWrapper<ConfigObject> getModuleConfig() {

        File moduleStorage = getConfigDirectory().resolve("MidnightCore").toFile();

        if(!moduleStorage.isDirectory() && !moduleStorage.mkdirs()) {
            throw new IllegalStateException("Unable to create module storage directory!");
        }

        return MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "modules", moduleStorage, new ConfigSection());
    }



    /**
     * An event fired when a player joins the proxy
     * @return The proxy's join event
     */
    HandlerList<ProxyPlayer> joinEvent();



    /**
     * An event fired when a player connects to a backend server
     * @return The proxy's shutdown event
     */
    HandlerList<Connection> connectEvent();


    /**
     * An event fired when a player leaves the proxy
     * @return The proxy's leave event
     */
    HandlerList<ProxyPlayer> leaveEvent();



    /**
     * An event fired when the proxy shuts down
     * @return The proxy's shutdown event
     */
    HandlerList<Proxy> shutdownEvent();

    /**
     * Contains the running proxy. Will be populated as soon as the proxy starts up.
     */
    Singleton<Proxy> RUNNING_PROXY = new Singleton<>();


    class Connection {
        public final ProxyPlayer player;
        public final ProxyServer server;
        public final ProxyServer previousServer;

        public Connection(ProxyPlayer player, ProxyServer server, ProxyServer previousServer) {
            this.player = player;
            this.server = server;
            this.previousServer = previousServer;
        }
    }

}
