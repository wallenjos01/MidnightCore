package org.wallentines.mcore;

import com.velocitypowered.api.proxy.server.RegisteredServer;

public class VelocityServer implements ProxyServer {

    private final RegisteredServer server;

    public VelocityServer(RegisteredServer server) {
        this.server = server;
    }

    @Override
    public String getAddress() {
        return null;
    }

    @Override
    public String getPort() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public RegisteredServer getInternal() {
        return server;
    }
}
