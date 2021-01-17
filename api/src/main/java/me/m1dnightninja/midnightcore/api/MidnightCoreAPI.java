package me.m1dnightninja.midnightcore.api;

import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.skin.Skin;

import java.util.HashMap;

public class MidnightCoreAPI {

    private static MidnightCoreAPI INSTANCE;
    private static ILogger LOGGER;

    private final ImplDelegate delegate;

    private final ConfigRegistry configRegistry;

    private final HashMap<String, IModule> loadedModules = new HashMap<>();

    public MidnightCoreAPI(ILogger logger, ImplDelegate delegate, IModule... modules) {
        if(INSTANCE == null) {
            INSTANCE = this;
            LOGGER = logger;
        }

        this.delegate = delegate;
        this.configRegistry = new ConfigRegistry();


        configRegistry.registerSerializer(Skin.class, Skin.SERIALIZER);


        for(IModule mod : modules) {
            if(loadedModules.containsKey(mod.getId())) {
                LOGGER.warn("Attempt to load Module with duplicate ID!");
                continue;
            }

            if(!mod.initialize()) {
                LOGGER.warn("Unable to initialize module " + mod.getId() + "!");
                continue;
            }

            loadedModules.put(mod.getId(), mod);
        }

        StringBuilder b = new StringBuilder("Enabled ");
        b.append(loadedModules.size());
        b.append(loadedModules.size() == 1 ? " module: " : " modules: ");

        int i = 1;
        for(String s : loadedModules.keySet()) {
            b.append(s);
            if(i < loadedModules.size()) {
                b.append(", ");
            }
            i++;
        }

        LOGGER.info(b.toString());

    }

    @SuppressWarnings("unchecked")
    public <T extends IModule> T getModule(Class<T> clazz) {
        for (IModule mod : loadedModules.values()) {
            if (clazz.isAssignableFrom(mod.getClass()) || clazz == mod.getClass()) {
                return (T) mod;
            }
        }
        return null;
    }

    public IModule getModuleById(String id) {
        return loadedModules.get(id);
    }

    public boolean isModuleLoaded(String id) {
        return loadedModules.containsKey(id);
    }

    public boolean areAllModulesLoaded(String... ids) {
        for(String s : ids) {
            if(!isModuleLoaded(s)) return false;
        }
        return true;
    }


    public AbstractTimer createTimer(String text, int seconds, boolean countUp, AbstractTimer.TimerCallback cb) {
        return delegate.createTimer(text, seconds, countUp, cb);
    }

    public AbstractInventoryGUI<?> createInventoryGUI(String title) {
        return delegate.createInventoryGUI(title);
    }


    public ConfigRegistry getConfigRegistry() {
        return configRegistry;
    }


    public static ILogger getLogger() {
        return LOGGER;
    }

    public static MidnightCoreAPI getInstance() {
        return INSTANCE;
    }

}
