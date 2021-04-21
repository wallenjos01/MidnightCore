package me.m1dnightninja.midnightcore.api;

import java.io.File;
import java.util.*;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractCustomScoreboard;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;

public class MidnightCoreAPI {

    private static MidnightCoreAPI INSTANCE;

    // Default logger
    private static ILogger LOGGER = new ILogger() {
        @Override
        public void info(String str) { System.out.println("[INFO] " + str); }

        @Override
        public void warn(String str) { System.out.println("[WARN] " + str); }

        @Override
        public void error(String str) { System.out.println("[ERROR] " + str); }
    };

    private static final ConfigRegistry configRegistry = new ConfigRegistry();


    private final ImplDelegate delegate;
    private final HashMap<MIdentifier, IModule> loadedModules = new HashMap<>();

    private final ConfigProvider defaultConfigProvider;
    private final ConfigSection mainConfig;

    private final Random rand;

    private final File dataFolder;

    public MidnightCoreAPI(ILogger logger, ImplDelegate delegate, ConfigProvider def, File dataFolder, IModule... modules) {

        // Register static variables if they haven't been already
        if (INSTANCE == null) {
            INSTANCE = this;
            LOGGER = logger;
        }

        // Register variables
        this.defaultConfigProvider = def;
        this.delegate = delegate;
        this.dataFolder = dataFolder;

        // Load config
        File configFile = new File(dataFolder, "config" + def.getFileExtension());
        if(!configFile.exists()) {
            def.saveToFile(new ConfigSection(), configFile);
        }

        this.mainConfig = def.loadFromFile(configFile);

        // Load module configs from
        ConfigSection moduleConfig = null;
        if(mainConfig.has("modules", ConfigSection.class)) {
            moduleConfig = mainConfig.getSection("modules");
        }

        if(moduleConfig == null) {
            moduleConfig = new ConfigSection();
            mainConfig.set("modules", moduleConfig);
            mainConfig.set("language", "en_us");
        }

        // Load list of disabled modules  from config.
        List<String> disabled = new ArrayList<>();
        if(mainConfig.has("disabled_modules", List.class)) {
            for(Object o : mainConfig.getList("disabled_modules")) {
                if(!(o instanceof String)) continue;
                disabled.add((String) o);
            }
        } else {
            mainConfig.set("disabled_modules", new ArrayList<>());
            def.saveToFile(mainConfig, configFile);
        }

        configRegistry.registerProvider(defaultConfigProvider);
        configRegistry.registerSerializer(Skin.class, Skin.SERIALIZER);
        configRegistry.registerSerializer(MItemStack.class, MItemStack.SERIALIZER);

        // Load modules
        for (IModule mod : modules) {

            // Filter disabled modules
            if(disabled.contains(mod.getId().toString())) continue;

            // Filter duplicate/conflicting modules
            if (this.loadedModules.containsKey(mod.getId())) {
                LOGGER.warn("Attempt to load Module with duplicate ID!");
                continue;
            }

            // Find out if modules depend on one another
            List<Class<? extends IModule>> deps = mod.getDependencies();
            if(deps != null) {
                int found = 0;
                for (IModule mod1 : modules) {
                    for (Class<?> clazz : deps) {
                        if(clazz.isAssignableFrom(mod1.getClass()) || clazz == mod1.getClass()) {
                            found++;
                        }
                        break;
                    }
                    if(found == deps.size()) {
                        break;
                    }
                }
                if(found < deps.size()) {
                    logger.warn("Unable to load module " + mod.getId() + "! Failed to find dependencies!");
                    continue;
                }
            }

            // Load configuration for module
            ConfigSection sec = null;
            ConfigSection defs = mod.getDefaultConfig();

            String modId = mod.getId().toString();

            if(moduleConfig.has(modId, ConfigSection.class)) {
                sec = moduleConfig.getSection(modId);
            }
            if(sec == null && defs != null) {
                sec = new ConfigSection();
                moduleConfig.set(modId, sec);
            }

            // Copy defaults
            if(defs != null) {
                sec.fill(defs);
            }

            // Initialize module
            if (!mod.initialize(sec)) {
                LOGGER.warn("Unable to initialize module " + mod.getId() + "!");
                continue;
            }
            this.loadedModules.put(mod.getId(), mod);
        }

        rand = new Random();

        // Save the main config again
        defaultConfigProvider.saveToFile(mainConfig, configFile);

        // Log a list of loaded modules
        StringBuilder b = new StringBuilder("Enabled ");
        b.append(this.loadedModules.size());
        b.append(this.loadedModules.size() == 1 ? " module: " : " modules: ");
        int i = 1;
        for (MIdentifier s : this.loadedModules.keySet()) {
            b.append(s.toString());
            if (i < this.loadedModules.size()) {
                b.append(", ");
            }
            ++i;
        }
        LOGGER.info(b.toString());
    }

    public File getDataFolder() {
        return dataFolder;
    }

    /**
     * Retrieves a loaded module based on its class
     *
     * @param clazz  The class or superclass of the module to find
     * @param <T>    A class that implements IModule
     * @return       The module with the given class
     */
    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> clazz) {
        for (IModule mod : this.loadedModules.values()) {
            if (!clazz.isAssignableFrom(mod.getClass()) && clazz != mod.getClass()) continue;
            return (T) mod;
        }
        return null;
    }

    /**
     * Returns the main config
     *
     * @return The main configuration
     */
    public ConfigSection getMainConfig() {
        return mainConfig;
    }

    /**
     * Returns the default configuration provider. (JSON for Fabric, YAML for spigot)
     *
     * @return The default config provider
     */
    public ConfigProvider getDefaultConfigProvider() {
        return defaultConfigProvider;
    }

    public Random getRandom() {
        return rand;
    }

    /**
     * Returns the module specified by the given ID.
     *
     * @param id  The ID of the module to find
     * @return    The module with the given ID
     */
    public IModule getModuleById(String id) {
        return this.loadedModules.get(MIdentifier.parse(id));
    }

    /**
     * Returns true if a module with the specified ID is loaded
     *
     * @param id  The ID of the module to find
     * @return    Whether or not the module is loaded
     */
    public boolean isModuleLoaded(String id) {

        for(MIdentifier mid : loadedModules.keySet()) {
            if(mid.toString().equals(id)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if there is a module loaded for each ID provided
     *
     * @param ids  The list of IDs to search for
     * @return     Whether or not all modules with the IDs are loaded
     */
    public boolean areAllModulesLoaded(String ... ids) {
        for (String s : ids) {
            if (this.isModuleLoaded(s)) continue;
            return false;
        }
        return true;
    }

    /**
     * Creates a timer with the given parameters
     *
     * @param text     The prefix of the timer
     * @param seconds  How long the timer should last (-1 for indefinite)
     * @param countUp  If the timer should count upwards instead of downward
     * @param cb       Callback to execute each tick
     * @return         A new timer object
     */
    public AbstractTimer createTimer(MComponent text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
        return this.delegate.createTimer(text, seconds, countUp, cb);
    }

    /**
     * Creates an Inventory GUI with a given title
     *
     * @param title  The title of the GUI
     * @return       The Inventory GUI
     */
    public AbstractInventoryGUI createInventoryGUI(MComponent title) {
        return this.delegate.createInventoryGUI(title);
    }

    /**
     * Creates a Title object that can be sent to players
     *
     * @param title The title itself
     * @param opts  Options pertaining to how the title is presented
     * @return      A new title object
     */
    public AbstractTitle createTitle(MComponent title, AbstractTitle.TitleOptions opts) { return this.delegate.createTitle(title, opts); }

    /**
     * Creates an ActionBar object that can be sent to players
     *
     * @param title The text of the ActionBar
     * @param opts  Options pertaining to how the ActionBar is presented
     * @return      A new ActionBar object
     */
    public AbstractActionBar createActionBar(MComponent title, AbstractActionBar.ActionBarOptions opts) { return this.delegate.createActionBar(title, opts); }

    /**
     * Creates a Scoreboard object that can be shown to players
     *
     * @param id    The ID of the objective to be sent. Should be unique and 16 characters or less
     * @param title The title of the scoreboard
     * @return      A new CustomScoreboard object
     */
    public AbstractCustomScoreboard createScoreboard(String id, MComponent title) { return this.delegate.createCustomScoreboard(id, title); }

    /**
     * Determines whether or not a player has a particular permission
     *
     * @param u          The user to query
     * @param permission The permission to check
     * @return           Whether or not a player has the permission
     */
    public boolean hasPermission(UUID u, String permission) {
        return delegate.hasPermission(u, permission);
    }

    /**
     * Retrieves the config registry
     *
     * @return The config registry
     */
    public static ConfigRegistry getConfigRegistry() {
        return configRegistry;
    }

    /**
     * Retrieves the logger
     *
     * @return The logger
     */
    public static ILogger getLogger() {

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

