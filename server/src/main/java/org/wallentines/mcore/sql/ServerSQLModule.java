package org.wallentines.mcore.sql;

import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class ServerSQLModule extends SQLModule implements ServerModule {
    @Override
    public boolean initialize(ConfigSection config, Server data) {

        init(config);
        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(ServerSQLModule::new, ID, new ConfigSection()).dependsOn(SQLModule.ID);
}
