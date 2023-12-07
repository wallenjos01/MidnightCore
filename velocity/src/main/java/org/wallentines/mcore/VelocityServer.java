package org.wallentines.mcore;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.net.InetSocketAddress;
import java.util.Collection;

public class VelocityServer implements ProxyServer {

    private final RegisteredServer server;
    private final Proxy proxy;

    public VelocityServer(RegisteredServer server, Proxy proxy) {
        this.server = server;
        this.proxy = proxy;
    }

    @Override
    public InetSocketAddress getAddress() {
        return server.getServerInfo().getAddress();
    }

    @Override
    public String getName() {
        return server.getServerInfo().getName();
    }

    @Override
    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public Collection<ProxyPlayer> getPlayers() {
        return server.getPlayersConnected().stream().map(pl -> proxy.getPlayer(pl.getUniqueId())).toList();
    }

    public RegisteredServer getInternal() {
        return server;
    }
}
