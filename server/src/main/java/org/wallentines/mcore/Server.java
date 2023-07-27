package org.wallentines.mcore;

import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.types.ResettableSingleton;

import java.nio.file.Path;
import java.util.Collection;
import java.util.UUID;

/**
 * An interface representing a running server
 */
public interface Server {

    /**
     * Finds a player with the given UUID
     * @param uuid The UUID to lookup
     * @return A player with the given UUID
     */
    Player getPlayer(UUID uuid);

    /**
     * Finds a player with the given username
     * @param name The username to lookup
     * @return A player with the given username
     */
    Player findPlayer(String name);

    /**
     * Gets a list of all players on the server
     * @return A list of all players on the server
     */
    Collection<Player> getPlayers();

    /**
     * Runs a command as the server console
     * @param command The command to run
     * @param quiet Whether output should be suppressed
     */
    void runCommand(String command, boolean quiet);

    /**
     * Runs a command as the server console
     * @param command The command to run
     */
    default void runCommand(String command) {
        runCommand(command, false);
    }

    /**
     * Returns whether the server is a dedicated server
     * @return whether the server is a dedicated server
     */
    boolean isDedicatedServer();

    /**
     * Returns the server's module manager
     * @return the server's module manager
     */
    ModuleManager<Server, ServerModule> getModuleManager();

    default void loadModules(Registry<ModuleInfo<Server, ServerModule>> registry) {

        ModuleManager<Server, ServerModule> manager = getModuleManager();
        manager.unloadAll();

        FileWrapper<ConfigObject> wrapper = MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "modules", getStorageDirectory().toFile(), new ConfigSection());
        manager.loadAll(wrapper.getRoot().asSection(), this, registry);

        wrapper.save();
    }

    Path getStorageDirectory();

    /**
     * Returns a reference to an event called every game tick
     * @return the server's tick event
     */
    HandlerList<Server> tickEvent();

    /**
     * Submits some code to be run on synchronously on the server thread
     * @param runnable The code to run
     */
    void submit(Runnable runnable);

    /**
     * A singleton carrying a reference to the currently running server. This is populated as soon as the server
     * begins to start and reset when the server is done shutting down
     */
    ResettableSingleton<Server> RUNNING_SERVER = new ResettableSingleton<>();

    /**
     * An event fired when the server finishes starting up
     */
    HandlerList<Server> START_EVENT = new SingletonHandlerList<>();

    /**
     * An event fired when the server starts shutting down
     */
    HandlerList<Server> STOP_EVENT = new SingletonHandlerList<>();


}
