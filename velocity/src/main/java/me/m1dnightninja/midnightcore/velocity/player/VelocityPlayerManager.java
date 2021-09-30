package me.m1dnightninja.midnightcore.velocity.player;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.MPlayerManager;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;

import java.util.UUID;

public class VelocityPlayerManager extends MPlayerManager {

    public VelocityPlayerManager() {

    }

    public void register(ProxyServer server) {

        server.getEventManager().register(MidnightCore.getInstance(), this);
    }

    @Subscribe
    private void onDisconnect(DisconnectEvent event) {
        cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @Override
    protected MPlayer createPlayer(UUID u) {

        return new VelocityPlayer(u);
    }
}
