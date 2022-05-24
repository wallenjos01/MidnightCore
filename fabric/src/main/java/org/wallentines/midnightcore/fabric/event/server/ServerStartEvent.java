package org.wallentines.midnightcore.fabric.event.server;

import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightlib.event.Event;

public class ServerStartEvent extends Event {

    private final MinecraftServer server;

    public ServerStartEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
