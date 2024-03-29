package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;


/**
 * Allows mods to implement and send custom packet types to clients
 */
public abstract class ServerMessagingModule implements ServerModule {

    protected final Registry<PacketHandler<Player>> handlers = new Registry<>(MidnightCoreAPI.MOD_ID);
    protected final Registry<PacketHandler<ServerLoginNegotiator>> loginHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);

    /**
     * An event fired when a Player begins connecting to the server, during the "Negotiating" phase. This event can
     * be used to implement custom negotiation on login
     */
    public final HandlerList<ServerLoginNegotiator> onLogin = new HandlerList<>();

    @Override
    public void disable() {
        for(Identifier id : handlers.getIds()) {
            doUnregister(id);
        }
        for(Identifier id : loginHandlers.getIds()) {
            doUnregisterLogin(id);
        }
    }

    /**
     * Sends a custom Packet to a player
     * @param player The player to send the data to
     * @param packet The packet to send
     */
    public void sendPacket(Player player, Packet packet) {

        player.getServer().submit(() -> {
            ByteBuf out = Unpooled.buffer();
            packet.write(out);
            sendPacket(player, packet.getId(), out);
        });
    }

    /**
     * Registers a custom Packet handler. The ID must be unique.
     * @param packetId The packet type's ID
     * @param handler The function which should handle the packet
     */
    public void registerPacketHandler(Identifier packetId, PacketHandler<Player> handler) {
        handlers.register(packetId, handler);
        doRegister(packetId);
    }

    /**
     * Registers a custom Packet handler for packets sent during the login state. The ID must be unique.
     * @param packetId The packet type's ID
     * @param handler The function which should handle the packet
     */
    public void registerLoginPacketHandler(Identifier packetId, PacketHandler<ServerLoginNegotiator> handler) {
        loginHandlers.register(packetId, handler);
        doRegisterLogin(packetId);
    }

    /**
     * Determines whether this module supports sending messages during the login phase. Will be false on Spigot servers
     * @return Whether this module supports sending custom login packets
     */
    public abstract boolean supportsLoginQuery();

    protected abstract void sendPacket(Player player, Identifier packetId, ByteBuf data);
    protected abstract void doRegister(Identifier packetId);
    protected abstract void doRegisterLogin(Identifier packetId);
    protected abstract void doUnregister(Identifier packetId);
    protected abstract void doUnregisterLogin(Identifier packetId);

    protected void handlePacket(Player sender, Identifier packetId, ByteBuf data) {
        if(!handlers.contains(packetId)) return;
        handlers.get(packetId).handle(sender, data);
    }

    protected void handleLoginPacket(ServerLoginNegotiator negotiator, Identifier packetId, ByteBuf data) {
        if(!loginHandlers.contains(packetId)) return;
        loginHandlers.get(packetId).handle(negotiator, data);
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messaging");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection()
            .with("udp_server", false)
            .with("udp_server_port", 25565);
}
