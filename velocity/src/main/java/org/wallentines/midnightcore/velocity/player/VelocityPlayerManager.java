package org.wallentines.midnightcore.velocity.player;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import org.wallentines.midnightcore.common.player.AbstractPlayer;
import org.wallentines.midnightcore.common.player.AbstractPlayerManger;
import org.wallentines.midnightcore.velocity.MidnightCore;

import java.util.Optional;
import java.util.UUID;

public class VelocityPlayerManager extends AbstractPlayerManger<Player> {

    public void register() {

        MidnightCore.getInstance().getServer().getEventManager().register(MidnightCore.getInstance(), this);
    }

    @Subscribe
    private void onConnect(ServerPreConnectEvent event) {
        cachePlayer(event.getPlayer().getUniqueId(), event.getPlayer());
    }

    @Subscribe(order = PostOrder.LAST)
    private void onDisconnect(DisconnectEvent event) {
        cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @Override
    protected AbstractPlayer<Player> createPlayer(UUID u) {
        return new VelocityPlayer(u);
    }

    @Override
    protected UUID toUUID(String name) {

        Optional<Player> opl =MidnightCore.getInstance().getServer().getPlayer(name);
        if(opl.isPresent()) return opl.get().getUniqueId();

        try {
            return UUID.fromString(name);
        } catch(IllegalArgumentException ex) {
            return null;
        }
    }

}
