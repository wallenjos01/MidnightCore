package org.wallentines.mcore.sql;

import net.minecraft.Util;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.DatabasePreset;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricServerSQLModule extends SQLModule implements ServerModule {

    @Override
    public boolean initialize(ConfigSection config, Server data) {

        init(config, Util.backgroundExecutor());
        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricServerSQLModule::new, ID, DEFAULT_CONFIG.copy()
            .with("presets", new ConfigSection()
                    .with("default",
                            new DatabasePreset("h2", "%server_config_dir%/MidnightCore/db", null, null, null, null, new ConfigSection()),
                            DatabasePreset.SERIALIZER
                    )
            ));
}
