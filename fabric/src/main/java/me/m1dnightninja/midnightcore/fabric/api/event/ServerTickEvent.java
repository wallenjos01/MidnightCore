package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.MinecraftServer;

public class ServerTickEvent extends Event {

    private final MinecraftServer server;
    private final int tickNumber;

    public ServerTickEvent(MinecraftServer server, int tickNumber) {
        this.server = server;
        this.tickNumber = tickNumber;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public int getTickNumber() {
        return tickNumber;
    }
}
