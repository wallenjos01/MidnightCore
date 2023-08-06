package org.wallentines.mcore;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.net.InetSocketAddress;

public class VelocityServer implements ProxyServer {

    private final RegisteredServer server;

    public VelocityServer(RegisteredServer server) {
        this.server = server;
    }

    @Override
    public InetSocketAddress getAddress() {
        return server.getServerInfo().getAddress();
    }

    @Override
    public String getName() {
        return server.getServerInfo().getName();
    }

    public RegisteredServer getInternal() {
        return server;
    }
}
