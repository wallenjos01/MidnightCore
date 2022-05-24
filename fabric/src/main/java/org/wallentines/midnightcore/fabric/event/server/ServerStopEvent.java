package org.wallentines.midnightcore.fabric.event.server;

import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightlib.event.Event;

public class ServerStopEvent extends Event {

    private final MinecraftServer server;

    public ServerStopEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
