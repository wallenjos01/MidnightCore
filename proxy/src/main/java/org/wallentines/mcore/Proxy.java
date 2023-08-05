package org.wallentines.mcore;

import org.wallentines.midnightlib.module.ModuleManager;

import java.nio.file.Path;

public interface Proxy {

    Path getConfigDirectory();

    ModuleManager<Proxy, ProxyModule> getModuleManager();

}
