package org.wallentines.midnightcore.fabric.module.session;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.module.session.Session;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.session.AbstractSessionModule;
import org.wallentines.midnightcore.fabric.event.player.PlayerLeaveEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerTickEvent;
import org.wallentines.midnightcore.fabric.module.savepoint.FabricSavepointModule;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricSessionModule extends AbstractSessionModule {

    @Override
    public boolean initialize(ConfigSection section, MServer data) {

        Event.register(ServerTickEvent.class, this, event -> this.tickAll());
        Event.register(ServerStopEvent.class, this, event -> shutdownAll());
        Event.register(PlayerLeaveEvent.class, this, 5, event -> {
            MPlayer mpl = FabricPlayer.wrap(event.getPlayer());
            Session sess = getSession(mpl);
            if(sess != null) sess.removePlayer(mpl);
        });
        return super.initialize(section, data);
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<MServer, ServerModule>(FabricSessionModule::new, ID, new ConfigSection()).dependsOn(FabricSavepointModule.ID);

}
