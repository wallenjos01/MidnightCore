package org.wallentines.mcore;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.wallentines.midnightlib.event.HandlerList;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.stream.Stream;

public class VelocityServer implements ProxyServer {

    private final RegisteredServer server;
    private final Proxy proxy;
    private final HandlerList<ProxyPlayer> onConnect = new HandlerList<>();

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
    public Stream<ProxyPlayer> getPlayers() {
        return server.getPlayersConnected().stream().map(pl -> proxy.getPlayer(pl.getUniqueId()));
    }

    @Override
    public int getPlayerCount() {
        return server.getPlayersConnected().size();
    }

    @Override
    public HandlerList<ProxyPlayer> connectEvent() {
        return onConnect;
    }

    public RegisteredServer getInternal() {
        return server;
    }
}
