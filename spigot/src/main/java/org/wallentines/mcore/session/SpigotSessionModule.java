package org.wallentines.mcore.session;

import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class SpigotSessionModule extends SessionModule {

    @Override
    public boolean initialize(ConfigSection section, Server data) {
        if(!super.initialize(section, data)) return false;

        data.joinEvent().register(this, this::loadRecovery);
        data.leaveEvent().register(this, spl -> {
            Session sess = getPlayerSession(spl);
            if(sess != null) sess.removePlayer(spl);
        });

        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(SpigotSessionModule::new, ID, new ConfigSection()).dependsOn(SavepointModule.ID);

}
