package org.wallentines.mcore.sql;

import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class ClientSQLModule extends SQLModule implements ClientModule {
    @Override
    public boolean initialize(ConfigSection config, Client data) {
        init(config);
        return true;
    }

    public static final ModuleInfo<Client, ClientModule> MODULE_INFO = new ModuleInfo<Client, ClientModule>(ClientSQLModule::new, ID, new ConfigSection()).dependsOn(SQLModule.ID);
}
