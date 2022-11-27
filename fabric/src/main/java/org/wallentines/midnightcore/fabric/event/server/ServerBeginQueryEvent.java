package org.wallentines.midnightcore.fabric.event.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.wallentines.midnightcore.fabric.module.messaging.FabricLoginNegotiator;
import org.wallentines.midnightlib.event.Event;

public class ServerBeginQueryEvent extends Event {

    private final MinecraftServer server;
    private final FabricLoginNegotiator negotiator;
    private final GameProfile profile;

    public ServerBeginQueryEvent(MinecraftServer server, GameProfile profile, FabricLoginNegotiator negotiator) {
        this.server = server;
        this.profile = profile;
        this.negotiator = negotiator;
    }

    public MinecraftServer getServer() {
        return server;
    }
    public FabricLoginNegotiator getNegotiator() {
        return negotiator;
    }
    public GameProfile getProfile() {
        return profile;
    }
}
