package org.wallentines.midnightcore.common.server;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;

public abstract class AbstractServer implements MServer {

    private final ModuleManager<MServer, ServerModule> moduleManager = new ModuleManager<>(Constants.DEFAULT_NAMESPACE);

    protected final MidnightCoreAPI api;

    protected final HandlerList<ServerEvent> tickEvent = new HandlerList<>();

    public AbstractServer(MidnightCoreAPI api) {
        this.api = api;
    }

    public void loadModules(ConfigSection config, Registry<ModuleInfo<MServer, ServerModule>> registry) {

        moduleManager.loadAll(config, this, registry);
        MidnightCoreAPI.getLogger().info("Loaded " + moduleManager.getCount() + " modules");
    }

    public void reloadModules(ConfigSection config, Registry<ModuleInfo<MServer, ServerModule>> registry) {
        moduleManager.unloadAll();
        loadModules(config, registry);
    }

    @Override
    public <T extends ServerModule> T getModule(Class<T> clazz) {
        return moduleManager.getModule(clazz);
    }

    @Override
    public ModuleManager<MServer, ServerModule> getModuleManager() {
        return moduleManager;
    }

    @Override
    public MidnightCoreAPI getMidnightCore() {
        return api;
    }


    @Override
    public HandlerList<ServerEvent> tickEvent() {
        return tickEvent;
    }
}
