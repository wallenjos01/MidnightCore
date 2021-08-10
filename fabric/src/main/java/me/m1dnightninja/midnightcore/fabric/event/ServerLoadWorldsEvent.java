package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.MinecraftServer;

public class ServerLoadWorldsEvent extends Event {

    private final MinecraftServer server;

    public ServerLoadWorldsEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return server;
    }
}
