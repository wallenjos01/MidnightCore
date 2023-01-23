package org.wallentines.midnightcore.client;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.client.module.ClientModule;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.FileConfig;
import org.wallentines.midnightlib.module.ModuleManager;

@SuppressWarnings("unused")
public class MidnightCoreClient {

    private static MidnightCoreClient INSTANCE = null;
    private final ModuleManager<MidnightCoreClient, ClientModule> modules = new ModuleManager<>();

    private final FileConfig config;

    public MidnightCoreClient() {

        MidnightCoreAPI.getLogger().info("Starting MidnightCore Client");

        if(INSTANCE == null) {
            INSTANCE = this;
        }

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        if(api == null) {
            throw new IllegalStateException("MidnightCore Client was initialized before MidnightCoreAPI!");
        }

        this.config = FileConfig.findOrCreate("client", api.getDataFolder());

    }

    public void loadModules() {

        ConfigSection sec = config.getRoot().getOrCreateSection("modules");
        modules.loadAll(sec, this, ClientRegistries.CLIENT_MODULE_REGISTRY);

        MidnightCoreAPI.getLogger().info("Loaded " + modules.getCount() + " client modules");
        config.save();

    }

    public ModuleManager<MidnightCoreClient, ClientModule> getModuleManager() {
        return modules;
    }

    public static MidnightCoreClient getInstance() {
        return INSTANCE;
    }

    public static <T extends ClientModule> T getModule(Class<T> clazz) {

        return INSTANCE.modules.getModule(clazz);
    }

}
