package org.wallentines.mcore;

import org.wallentines.mcore.util.ModuleUtil;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.module.ModuleManager;
import org.wallentines.midnightlib.registry.Registry;
import org.wallentines.midnightlib.types.Singleton;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public interface Proxy {

    Path getConfigDirectory();

    ModuleManager<Proxy, ProxyModule> getModuleManager();

    ProxyPlayer getPlayer(UUID uuid);

    ProxyServer getServer(String name);

    /**
     * Loads all modules from the given registry using the server's module config
     * @param registry The registry to find modules in
     */
    default void loadModules(Registry<ModuleInfo<Proxy, ProxyModule>> registry) {

        File moduleStorage = getConfigDirectory().resolve("MidnightCore").toFile();

        ModuleUtil.loadModules(getModuleManager(), registry, this, moduleStorage);

        shutdownEvent().register(this, ev -> getModuleManager().unloadAll());

    }

    HandlerList<Proxy> shutdownEvent();

    Singleton<Proxy> RUNNING_PROXY = new Singleton<>();

}
