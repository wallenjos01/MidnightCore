package org.wallentines.mcore.sql;

import io.netty.util.concurrent.ThreadPerTaskExecutor;
import org.wallentines.mcore.Proxy;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.DatabasePreset;
import org.wallentines.midnightlib.module.ModuleInfo;

public class VelocitySQLModule extends SQLModule implements ProxyModule {
    @Override
    public boolean initialize(ConfigSection config, Proxy data) {
        init(config, new PlaceholderContext().withValue(data), new ThreadPerTaskExecutor(Thread::new));
        return true;
    }

    public static final ModuleInfo<Proxy, ProxyModule> MODULE_INFO = new ModuleInfo<>(VelocitySQLModule::new, ID, DEFAULT_CONFIG.copy()
            .with("presets", new ConfigSection()
                    .with("default",
                            new DatabasePreset("h2", "%proxy_config_dir%/MidnightCore/db", null, null, null, null, new ConfigSection()),
                            DatabasePreset.SERIALIZER
                    )
            ));
}
