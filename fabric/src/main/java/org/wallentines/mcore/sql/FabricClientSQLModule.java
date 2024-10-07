package org.wallentines.mcore.sql;

import net.minecraft.Util;
import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.sql.DatabasePreset;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricClientSQLModule extends SQLModule implements ClientModule {
    @Override
    public boolean initialize(ConfigSection config, Client data) {
        init(config, Util.backgroundExecutor());
        return true;
    }

    public static final ModuleInfo<Client, ClientModule> MODULE_INFO = new ModuleInfo<>(FabricClientSQLModule::new, ID, DEFAULT_CONFIG.copy()
            .with("presets", new ConfigSection()
                    .with("default",
                            new DatabasePreset("h2", "%client_config_dir%/MidnightCore/db", null, null, null, null, new ConfigSection()),
                            DatabasePreset.SERIALIZER
                    )
            ));
}
