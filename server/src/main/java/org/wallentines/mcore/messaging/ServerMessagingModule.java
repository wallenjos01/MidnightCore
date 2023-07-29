package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

public abstract class ServerMessagingModule implements ServerModule {

    private final Registry<ServerboundPacketHandler> handlers = new Registry<>(MidnightCoreAPI.MOD_ID);

    public abstract void sendPacket(Player player, Identifier packetId, ByteBuf data);

    public void registerHandler(Identifier packetId, ServerboundPacketHandler handler) {
        handlers.register(packetId, handler);
    }

    protected void handlePacket(Player sender, Identifier packetId, ByteBuf data) {

        if(!handlers.contains(packetId)) return;
        handlers.get(packetId).handle(sender, data);

    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messaging");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("udp_server", false)
            .with("udp_server_port", 25565);
}
