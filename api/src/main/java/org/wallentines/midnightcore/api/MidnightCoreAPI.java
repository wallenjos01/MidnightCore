package org.wallentines.midnightcore.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightcore.api.item.InventoryGUI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.player.PlayerManager;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.requirement.RequirementType;

import java.io.File;
import java.util.Random;

@SuppressWarnings("unused")
public abstract class MidnightCoreAPI {

    private static final Logger LOGGER = LogManager.getLogger("MidnightCore");
    private static MidnightCoreAPI INSTANCE;

    protected MidnightCoreAPI() {
        if(INSTANCE == null) INSTANCE = this;
    }

    /**
     * Returns the main config
     *
     * @return The main configuration
     */
    public abstract ConfigSection getConfig();

    /**
     * Saves the main config to disk
     */
    public abstract void saveConfig();


    /**
     * Gets the plugin/mod's data folder, where all configuration is stored.
     * Typically, "plugins/MidnightCore" on Spigot, and "config/MidnightCore" on Fabric
     *
     * @return The data folder as a File object
     */
    public abstract File getDataFolder();

    /**
     * Returns the version of Minecraft the server is running
     *
     * @return The version
     */
    public abstract Version getGameVersion();

    /**
     * Returns the main module manager
     *
     * @return The module manager
     */
    public abstract ModuleManager<MServer, ServerModule> getModuleManager();

    /**
     * Returns the requirement type registry, so additional requirement types can be added
     *
     * @return The requirement type registry
     */
    public abstract Registry<RequirementType<MPlayer>> getRequirementRegistry();

    /**
     * Returns the mod's player manager, which contains a player object for each online player.
     *
     * @return The player manager
     */
    @Deprecated
    public abstract PlayerManager getPlayerManager();

    /**
     * Creates a new item stack given an id, count, and NBT tag
     *
     * @param id    The type of item to create (e.g. minecraft:diamond)
     * @param count The count of the item stack
     * @param nbt   The NBT tag to be applied to the item stack
     *
     * @return The version
     */
    public abstract MItemStack createItem(Identifier id, int count, ConfigSection nbt);

    /**
     * Creates an Inventory GUI with a given title
     *
     * @param title  The title of the GUI
     * @return       The Inventory GUI
     */
    public abstract InventoryGUI createGUI(MComponent title);

    /**
     * Creates a Custom Scoreboard with a given title
     *
     * @param title The title of the scoreboard
     * @return      The scoreboard
     */
    public abstract CustomScoreboard createScoreboard(String id, MComponent title);

    /**
     * Executes a command in the server console
     *
     * @param command The command to run
     */
    @Deprecated
    public void executeConsoleCommand(String command) {
        executeConsoleCommand(command, true);
    }

    /**
     * Executes a command in the server console
     *
     * @param command The command to run
     * @param log     Whether the output should be logged or sent to admins
     */
    @Deprecated
    public abstract void executeConsoleCommand(String command, boolean log);

    /**
     * Submits a function to be run synchronously on the server thread on the next tick
     * @param runnable The function to run
     */
    @Deprecated
    public abstract void executeOnServer(Runnable runnable);

    /**
     * Retrieves the global Random object
     *
     * @return The global random object
     */
    public abstract Random getRandom();

    /**
     * Retrieves the default locale used by the server
     *
     * @return The default locale used by the server
     */
    public abstract String getServerLocale();

    /**
     * Retrieves the currently running server. This may return null on clients in the main menu, or on servers
     * during startup before the MinecraftServer object has been created.
     * @return The currently running server
     */
    public abstract MServer getServer();

    /**
     * Reloads the main config and all registered modules
     */
    public abstract void reload();

    /**
     * Disables all loaded modules
     */
    @Deprecated
    public abstract void unloadModules();

    /**
     * Retrieves the lang provider used by MidnightCore commands
     * @return The MidnightCore lang provider
     */
    public abstract LangProvider getLangProvider();

    /**
     * Returns the global MidnightCoreAPI instance. Will be null if the instance has not been created yet
     *
     * @return The global api
     */
    @Nullable
    public static MidnightCoreAPI getInstance() {
        return INSTANCE;
    }

    /**
     * Returns the global MidnightCoreAPI logger
     *
     * @return The logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * Gets a module from the global MidnightCoreAPI instance. Will be null if the API has not been created yet or the module is not enabled or the module has been unloaded
     *
     * @param clazz The class of the module to look up
     * @return A module with the given class, or null
     * @param <T> The type of module to look up
     */
    @Nullable
    public static <T extends ServerModule> T getModule(Class<T> clazz) {

        MidnightCoreAPI inst = getInstance();
        if(inst == null) return null;

        return inst.getModuleManager().getModule(clazz);
    }

    /**
     * Gets a reference to the server that is currently running. This may be null before the server has started, or for clients in the main menu
     * @return The running server
     */
    @Nullable
    public static MServer getRunningServer() {

        MidnightCoreAPI inst = getInstance();
        if(inst == null) return null;

        return inst.getServer();
    }

}
