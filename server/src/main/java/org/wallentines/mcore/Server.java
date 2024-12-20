package org.wallentines.mcore;

import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.FileWrapper;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.event.SingletonHandlerList;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.CheckType;
import org.wallentines.midnightlib.requirement.CompositeCheck;
import org.wallentines.midnightlib.requirement.Requirement;
import org.wallentines.midnightlib.requirement.StringCheck;
import org.wallentines.midnightlib.types.ResettableSingleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

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
    Stream<Player> getPlayers();

    /**
     * Gets the number of online players
     * @return The number of online players
     */
    int getPlayerCount();


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

    /**
     * Loads all server modules using the server's module config
     */
    default void loadModules() {

        ModuleUtil.loadModules(getModuleManager(), getModuleConfig());
        shutdownEvent().register(this, ev -> getModuleManager().unloadAll());
    }

    /**
     * Gets the directory where the server stores files. Will be in the plugins directory for spigot servers, the
     * config directory for dedicated servers, and a config folder in the world directory for integrated servers.
     * @return The directory where the server stores files
     */
    Path getConfigDirectory();

    /**
     * Gets the configuration file for modules
     * @return The module config
     */
    default FileWrapper<ConfigObject> getModuleConfig() {

        Path moduleStorage = getConfigDirectory().resolve("MidnightCore");

        if(!Files.isDirectory(moduleStorage)) {
            try {
                Files.createDirectories(moduleStorage);
            } catch (IOException ex) {
                throw new RuntimeException("Unable to create module storage directory!", ex);
            }
        }

        return MidnightCoreAPI.FILE_CODEC_REGISTRY.findOrCreate(ConfigContext.INSTANCE, "modules", moduleStorage, new ConfigSection());
    }

    /**
     * An event called every game tick
     * @return the server's tick event
     */
    HandlerList<Server> tickEvent();

    /**
     * An event called when the server shuts down
     * @return The server's shutdown event
     */
    HandlerList<Server> shutdownEvent();

    /**
     * Called when a player joins the game
     * @return The server's join event
     */
    HandlerList<Player> joinEvent();

    /**
     * Called when a player leaves the game
     * @return The server's leave event
     */
    HandlerList<Player> leaveEvent();


    /**
     * Submits some code to be run on synchronously on the server thread
     * @param runnable The code to run
     */
    void submit(Runnable runnable);

    /**
     * Gets the version of the game the server is running
     * @return The server's game version
     */
    GameVersion getVersion();

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


    static void registerPlaceholders(PlaceholderManager manager) {

        manager.registerSupplier("server_modules_loaded", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Server.class, srv -> srv.getModuleManager().getCount() + "", "0")));
        manager.registerSupplier("server_player_count", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Server.class, srv -> srv.getPlayerCount() + "", "0")));
        manager.registerSupplier("server_modules_registered", PlaceholderSupplier.inline(ctx -> ServerModule.REGISTRY.getSize() + ""));
        manager.registerSupplier("server_config_dir", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(Server.class, srv -> srv.getConfigDirectory().toString(), "")));

    }

    Registry<Identifier, CheckType<Player, ?>> REQUIREMENT_REGISTRY = Registry.create(MidnightCoreAPI.MOD_ID);


}
