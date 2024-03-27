package org.wallentines.mcore.extension;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.messaging.ServerMessagingModule;
import org.wallentines.midnightlib.module.ModuleInfo;

import java.util.function.Consumer;

public class FabricServerExtensionModule extends ServerExtensionModule {
    @Override
    protected void registerJoinListener(Consumer<Player> player) {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            player.accept(handler.getPlayer());
        });
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<Server, ServerModule>(FabricServerExtensionModule::new, ID, DEFAULT_CONFIG).dependsOn(ServerMessagingModule.ID);

}
