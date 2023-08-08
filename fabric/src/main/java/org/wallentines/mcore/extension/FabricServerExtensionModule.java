package org.wallentines.mcore.extension;

import org.wallentines.fbev.player.PlayerJoinEvent;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.messaging.ServerMessagingModule;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.function.Consumer;

public class FabricServerExtensionModule extends ServerExtensionModule {
    @Override
    protected void registerJoinListener(Consumer<Player> player) {
        Event.register(PlayerJoinEvent.class, this, ev -> player.accept(ev.getPlayer()));
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(FabricServerExtensionModule::new, ID, DEFAULT_CONFIG).dependsOn(ServerMessagingModule.ID);

}
