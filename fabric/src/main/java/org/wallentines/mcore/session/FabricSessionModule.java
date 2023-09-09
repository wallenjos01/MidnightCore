package org.wallentines.mcore.session;

import org.wallentines.fbev.player.PlayerJoinEvent;
import org.wallentines.fbev.player.PlayerLeaveEvent;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.savepoint.SavepointModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricSessionModule extends SessionModule {

    @Override
    public boolean initialize(ConfigSection section, Server data) {
        if(!super.initialize(section, data)) return false;

        Event.register(PlayerLeaveEvent.class, this, 10, ev -> {
            Session sess = getPlayerSession((Player) ev.getPlayer());
            if(sess != null) sess.removePlayer((Player) ev.getPlayer());
        });
        Event.register(PlayerJoinEvent.class, this, 20, ev -> {
            loadRecovery((Player) ev.getPlayer());
        });

        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(FabricSessionModule::new, ID, new ConfigSection()).dependsOn(SavepointModule.ID);
}
