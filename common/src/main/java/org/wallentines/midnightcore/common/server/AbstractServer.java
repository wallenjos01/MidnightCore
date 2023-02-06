package org.wallentines.midnightcore.common.server;

import org.wallentines.midnightcore.api.FileConfig;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;

public abstract class AbstractServer implements MServer {

    private final ModuleManager<MServer, ServerModule> moduleManager = new ModuleManager<>(MidnightCoreAPI.DEFAULT_NAMESPACE);

    protected final MidnightCoreAPI api;
    protected final FileConfig moduleConfig;

    protected final HandlerList<ServerEvent> tickEvent = new HandlerList<>();

    public AbstractServer(MidnightCoreAPI api, FileConfig moduleConfig) {
        this.api = api;
        this.moduleConfig = moduleConfig;
    }

    public void loadModules(Registry<ModuleInfo<MServer, ServerModule>> registry) {

        moduleManager.loadAll(moduleConfig.getRoot(), this, registry);
        moduleConfig.save();
        MidnightCoreAPI.getLogger().info("Loaded " + moduleManager.getCount() + " modules");
    }

    public void reloadModules(Registry<ModuleInfo<MServer, ServerModule>> registry) {
        moduleManager.unloadAll();
        loadModules(registry);
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
    public FileConfig getModuleConfig() {
        return moduleConfig;
    }

    @Override
    public HandlerList<ServerEvent> tickEvent() {
        return tickEvent;
    }

    protected static FileConfig getDedicatedConfigPath(MidnightCoreAPI api) {
        return FileConfig.findOrCreate("modules", api.getDataFolder());
    }
}
