package org.wallentines.mcore.session;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricSessionModule extends SessionModule {

    @Override
    public boolean initialize(ConfigSection section, Server data) {
        if(!super.initialize(section, data)) return false;

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            loadRecovery(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            Session sess = getPlayerSession(handler.getPlayer());
            if(sess != null) sess.removePlayer(handler.getPlayer());
        });


        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(FabricSessionModule::new, ID, new ConfigSection()).dependsOn(SavepointModule.ID);
}
