package me.m1dnightninja.midnightcore.api;

import java.io.File;
import java.util.HashMap;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.skin.Skin;

public class MidnightCoreAPI {

    private static MidnightCoreAPI INSTANCE;
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
    private final HashMap<String, IModule> loadedModules = new HashMap<>();

    private final ConfigProvider defaultConfigProvider;

    private final ConfigSection mainConfig;

    public MidnightCoreAPI(ILogger logger, ImplDelegate delegate, ConfigProvider def, File configFile, ConfigSection config, IModule... modules) {
        if (INSTANCE == null) {
            INSTANCE = this;
            LOGGER = logger;
        }

        this.defaultConfigProvider = def;
        this.delegate = delegate;
        this.mainConfig = config;

        ConfigSection moduleConfig = null;
        if(mainConfig.has("modules", ConfigSection.class)) {
            moduleConfig = mainConfig.getSection("modules");
        }

        if(moduleConfig == null) {
            moduleConfig = new ConfigSection();
            mainConfig.set("modules", moduleConfig);
        }

        configRegistry.registerSerializer(Skin.class, Skin.SERIALIZER);
        for (IModule mod : modules) {

            if (this.loadedModules.containsKey(mod.getId())) {
                LOGGER.warn("Attempt to load Module with duplicate ID!");
                continue;
            }

            ConfigSection sec = null;
            ConfigSection defs = mod.getDefaultConfig();

            if(moduleConfig.has(mod.getId(), ConfigSection.class)) {
                sec = moduleConfig.getSection(mod.getId());
            }
            if(sec == null && defs != null) {
                sec = new ConfigSection();
                moduleConfig.set(mod.getId(), sec);
            }

            if(defs != null) {
                sec.fill(defs);
            }

            if (!mod.initialize(sec)) {
                LOGGER.warn("Unable to initialize module " + mod.getId() + "!");
                continue;
            }
            this.loadedModules.put(mod.getId(), mod);
        }

        defaultConfigProvider.saveToFile(mainConfig, configFile);

        StringBuilder b = new StringBuilder("Enabled ");
        b.append(this.loadedModules.size());
        b.append(this.loadedModules.size() == 1 ? " module: " : " modules: ");
        int i = 1;
        for (String s : this.loadedModules.keySet()) {
            b.append(s);
            if (i < this.loadedModules.size()) {
                b.append(", ");
            }
            ++i;
        }
        LOGGER.info(b.toString());
    }

    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> clazz) {
        for (IModule mod : this.loadedModules.values()) {
            if (!clazz.isAssignableFrom(mod.getClass()) && clazz != mod.getClass()) continue;
            return (T) mod;
        }
        return null;
    }

    public ConfigProvider getDefaultConfigProvider() {
        return defaultConfigProvider;
    }

    public IModule getModuleById(String id) {
        return this.loadedModules.get(id);
    }

    public boolean isModuleLoaded(String id) {
        return this.loadedModules.containsKey(id);
    }

    public boolean areAllModulesLoaded(String ... ids) {
        for (String s : ids) {
            if (this.isModuleLoaded(s)) continue;
            return false;
        }
        return true;
    }

    public AbstractTimer createTimer(String text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
        return this.delegate.createTimer(text, seconds, countUp, cb);
    }

    public AbstractInventoryGUI<?> createInventoryGUI(String title) {
        return this.delegate.createInventoryGUI(title);
    }

    public static ConfigRegistry getConfigRegistry() {
        return configRegistry;
    }

    public static ILogger getLogger() {

        return LOGGER;
    }

    public static MidnightCoreAPI getInstance() {
        return INSTANCE;
    }
}

