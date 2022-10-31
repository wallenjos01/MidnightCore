package org.wallentines.midnightcore.fabric.module.session;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.common.module.session.AbstractSessionModule;
import org.wallentines.midnightcore.fabric.event.server.ServerStartEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerStopEvent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

public class FabricSessionModule extends AbstractSessionModule {

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {
        Event.register(ServerStartEvent.class, this, event -> {
            event.getServer().addTickable(this::tickAll);
        });
        Event.register(ServerStopEvent.class, this, event -> {
            shutdownAll();
        });
        return super.initialize(section, data);
    }

    @Override
    public void disable() {
        Event.unregisterAll(this);
    }

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricSessionModule::new, ID, new ConfigSection());

}
