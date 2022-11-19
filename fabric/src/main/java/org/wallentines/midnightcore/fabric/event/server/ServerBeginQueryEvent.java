package org.wallentines.midnightcore.fabric.event.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightcore.fabric.module.extension.ServerNegotiator;
import org.wallentines.midnightlib.event.Event;

public class ServerBeginQueryEvent extends Event {

    private final MinecraftServer server;
    private final ServerNegotiator negotiator;
    private final GameProfile profile;

    public ServerBeginQueryEvent(MinecraftServer server, GameProfile profile, ServerNegotiator negotiator) {
        this.server = server;
        this.profile = profile;
        this.negotiator = negotiator;
    }

    public MinecraftServer getServer() {
        return server;
    }
    public ServerNegotiator getNegotiator() {
        return negotiator;
    }
    public GameProfile getProfile() {
        return profile;
    }
}
