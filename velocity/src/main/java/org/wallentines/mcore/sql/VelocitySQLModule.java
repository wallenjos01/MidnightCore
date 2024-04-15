package org.wallentines.mcore.sql;

import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class VelocitySQLModule extends SQLModule implements ProxyModule {
    @Override
    public boolean initialize(ConfigSection config, Proxy data) {
        init(config, new ThreadPerTaskExecutor(Thread::new));
        return true;
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<>(VelocitySQLModule::new, ID, DEFAULT_CONFIG);
}
