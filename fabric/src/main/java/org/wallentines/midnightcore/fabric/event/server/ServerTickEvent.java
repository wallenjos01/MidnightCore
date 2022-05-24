package org.wallentines.midnightcore.fabric.event.server;

import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightlib.event.Event;

public class ServerTickEvent extends Event {

    private final MinecraftServer server;
    private final int tickCount;

    public ServerTickEvent(MinecraftServer server, int tickCount) {
        this.server = server;
        this.tickCount = tickCount;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public int getTickCount() {
        return tickCount;
    }
}
