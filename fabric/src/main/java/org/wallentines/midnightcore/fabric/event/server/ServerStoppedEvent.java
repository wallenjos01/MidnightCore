package org.wallentines.midnightcore.fabric.event.server;

import net.minecraft.server.MinecraftServer;

public class ServerStoppedEvent {

    private final MinecraftServer server;

    public ServerStoppedEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
