package org.wallentines.mcore;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.mcore.text.Component;

import java.util.UUID;

public class VelocityPlayer implements ProxyPlayer {

    private final Player player;

    public VelocityPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public String getUsername() {
        return player.getUsername();
    }

    @Override
    public void sendMessage(Component message) {

    }

    @Override
    public void sendToServer(ProxyServer server) {
        RegisteredServer rs = ((VelocityServer) server).getInternal();
        player.createConnectionRequest(rs).fireAndForget();
    }

    @Override
    public ProxyServer getServer() {
        return null;
    }
}
