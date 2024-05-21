package org.wallentines.mcore.sql;

import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SpigotSQLModule extends SQLModule implements ServerModule {

    @Override
    public boolean initialize(ConfigSection config, Server data) {

        init(config, new ThreadPoolExecutor(1, 8, 10000, TimeUnit.MILLISECONDS, new SynchronousQueue<>()));
        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(SpigotSQLModule::new, ID, DEFAULT_CONFIG.copy()
            .with("presets", new ConfigSection()
                    .with("default", new DatabasePreset(
                            UnresolvedComponent.completed(Component.text("h2")),
                            UnresolvedComponent.parse("%mcore_config_dir%/db").getOrThrow(),
                            null,
                            null,
                            null,
                            null,
                            new ConfigSection()
                    ), DatabasePreset.SERIALIZER)));

}
