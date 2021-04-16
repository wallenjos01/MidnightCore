package me.m1dnightninja.midnightcore.fabric.module;

import me.m1dnightninja.midnightcore.common.module.AbstractPlayerDataModule;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class PlayerDataModule extends AbstractPlayerDataModule {

    @Override
    protected void registerListeners() {

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> onShutdown());
        Event.register(PlayerDisconnectEvent.class, this, event -> onLeave(event.getPlayer().getUUID()));

    }
}
