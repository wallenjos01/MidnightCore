package me.m1dnightninja.midnightcore.velocity.module.playerdata;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import me.m1dnightninja.midnightcore.common.module.playerdata.AbstractPlayerDataModule;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;

public class PlayerDataModule extends AbstractPlayerDataModule {

    @Override
    protected void registerListeners() {

        ProxyServer server = MidnightCore.getInstance().getServer();
        server.getEventManager().register(MidnightCore.getInstance(), this);

    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onShutdown(ProxyShutdownEvent event) {
        onShutdown();
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onLeave(DisconnectEvent event) {
        onLeave(event.getPlayer().getUniqueId());
    }
}
