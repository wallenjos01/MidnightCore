package org.wallentines.mcore.sql;

import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class ProxySQLModule extends SQLModule implements ProxyModule {
    @Override
    public boolean initialize(ConfigSection config, Proxy data) {
        init(config);
        return true;
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<>(ProxySQLModule::new, ID, DEFAULT_CONFIG);
}
