package me.m1dnightninja.midnightcore.api;

import java.io.File;
import java.util.*;

import me.m1dnightninja.midnightcore.api.config.*;
import me.m1dnightninja.midnightcore.api.inventory.MInventoryGUI;
import me.m1dnightninja.midnightcore.api.inventory.ItemConverter;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.module.IModuleRegistry;
import me.m1dnightninja.midnightcore.api.player.MPlayerManager;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MidnightCoreAPI {

    protected static final Logger LOGGER = LogManager.getLogger("MidnightCore");

    protected static MidnightCoreAPI INSTANCE;

    protected MidnightCoreAPI() {

        if(INSTANCE == null) {
            INSTANCE = this;
        }
    }

    /**
     * Gets the plugin/mod's data folder, where all configuration is stored.
     * Typically, "plugins/MidnightCore" on Spigot, and "config/MidnightCore" on Fabric
     *
     * @return The data folder as a File object
     */
    public abstract File getDataFolder();

    /**
     * Retrieves a loaded module based on its class
     *
     * @param clazz  The class or superclass of the module to find
     * @param <T>    A class that implements IModule
     * @return       The module with the given class
     */
    public abstract <T extends IModule> T getModule(Class<T> clazz);

    /**
     * Returns the main config
     *
     * @return The main configuration
     */
    public abstract ConfigSection getMainConfig();

    /**
     * Returns the default configuration provider. (Typically JSON for Fabric, YAML for spigot)
     *
     * @return The default config provider
     */
    public abstract ConfigProvider getDefaultConfigProvider();

    /**
     * Returns the global Random object.
     *
     * @return A Random instance
     */
    public abstract Random getRandom();

    /**
     * Returns the module specified by the given ID.
     *
     * @param id  The ID of the module to find
     * @return    The module with the given ID
     */
    public abstract IModule getModuleById(MIdentifier id);

    /**
     * Returns the module specified by the given ID.
     *
     * @param id  The ID of the module to find as a String
     * @return    The module with the given ID
     */
    public final IModule getModuleById(String id) {

        return getModuleById(MIdentifier.parse(id));
    }

    /**
     * Returns true if a module with the specified ID is loaded
     *
     * @param id  The ID of the module to find
     * @return    Whether the module is loaded
     */
    public abstract boolean isModuleLoaded(MIdentifier id);

    /**
     * Returns true if a module with the specified ID is loaded
     *
     * @param id  The ID of the module to find as a String
     * @return    Whether the module is loaded
     */
    public final boolean isModuleLoaded(String id) {

        return isModuleLoaded(MIdentifier.parse(id));
    }

    /**
     * Returns true if there is a module loaded for each ID provided
     *
     * @param ids  The list of IDs to search for
     * @return     Whether all modules with the IDs are loaded
     */
    public final boolean areAllModulesLoaded(String ... ids) {
        for (String s : ids) {
            if (!this.isModuleLoaded(s)) return false;
        }
        return true;
    }

    /**
     * Returns the Module Registry associated with this instance of the API
     *
     * @return The Module Registry
     */
    public abstract IModuleRegistry getModuleRegistry();

    /**
     * Creates a timer with the given parameters
     *
     * @param text     The prefix of the timer
     * @param seconds  How long the timer should last (-1 for indefinite)
     * @param countUp  If the timer should count upwards instead of downward
     * @param cb       Callback to execute each tick
     * @return         A new timer object
     */
    public abstract MTimer createTimer(MComponent text, int seconds, boolean countUp, MTimer.TimerCallback cb);

    /**
     * Creates an Inventory GUI with a given title
     *
     * @param title  The title of the GUI
     * @return       The Inventory GUI
     */
    public abstract MInventoryGUI createInventoryGUI(MComponent title);

    /**
     * Creates a Scoreboard object that can be shown to players
     *
     * @param id    The ID of the objective to be sent. Should be unique and 16 characters or fewer
     * @param title The title of the scoreboard
     * @return      A new CustomScoreboard object
     */
    public abstract MScoreboard createScoreboard(String id, MComponent title);

    /**
     * Runs a console command on the server
     *
     * @param cmd The command to execute
     */
    public abstract void executeConsoleCommand(String cmd);

    /**
     * Returns the global Player Manager object
     *
     * @return The PlayerManager
     */
    public abstract MPlayerManager getPlayerManager();

    /**
     * Returns the global Item Converter object
     *
     * @return The ItemConverter
     */
    public abstract ItemConverter getItemConverter();

    /**
     * Retrieves the config registry
     *
     * @return The config registry
     */
    public abstract ConfigRegistry getConfigRegistry();

    /**
     * Retrieves the logger
     *
     * @return The logger
     */
    public static Logger getLogger() {

        return LOGGER;
    }

    /**
     * Retrieves the instance of the API, created once by a plugin/mod
     *
     * @return The main instance of MidnightCore
     */
    public static MidnightCoreAPI getInstance() {
        return INSTANCE;
    }

}