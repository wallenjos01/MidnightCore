package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.ProxyModule;
import org.wallentines.mcore.ProxyPlayer;
import org.wallentines.mcore.ProxyServer;
import org.wallentines.midnightlib.event.HandlerList;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

/**
 * A module for sending custom packets to players and servers.
 */
public abstract class ProxyPluginMessageModule implements ProxyModule {

    protected final Registry<Identifier, PacketHandler<ProxyPlayer>> playerHandlers = Registry.create(MidnightCoreAPI.MOD_ID);
    protected final Registry<Identifier, PacketHandler<ServerMessage>> serverHandlers = Registry.create(MidnightCoreAPI.MOD_ID);
    protected final Registry<Identifier, PacketHandler<ProxyPlayer>> loginHandlers = Registry.create(MidnightCoreAPI.MOD_ID);

    /**
     * An event called when a player logs into the proxy. (Not individual servers)
     */
    public final HandlerList<ProxyLoginNegotiator> onLogin = new HandlerList<>();

    /**
     * Sends a custom packet to a player
     * @param player The player to send the packet
     * @param packet The packet to send
     */
    public void sendPlayerMessage(ProxyPlayer player, Packet packet) {
        ByteBuf out = Unpooled.buffer();
        packet.write(out);
        sendPlayerMessage(player, packet.getId(), out);
    }

    /**
     * Sends a custom packet to a server. This will not work if the server has no player connected to it.
     * @param player A player on the server to send a packet to
     * @param packet The packet to send
     */
    public void sendServerMessage(ProxyPlayer player, Packet packet) {
        ByteBuf out = Unpooled.buffer();
        packet.write(out);
        sendServerMessage(player.getServer(), packet.getId(), out);
    }


    /**
     * Sends a custom packet to a server. This will not work if the server has no player connected to it.
     * @param server The server to send a packet to
     * @param packet The packet to send
     */
    public void sendServerMessage(ProxyServer server, Packet packet) {

        ByteBuf out = Unpooled.buffer();
        packet.write(out);
        sendServerMessage(server, packet.getId(), out);
    }



    protected abstract void sendPlayerMessage(ProxyPlayer player, Identifier id, ByteBuf out);
    protected abstract void sendServerMessage(ProxyServer server, Identifier id, ByteBuf out);

    /**
     * Registers a handler for custom packets sent from clients
     * @param id The ID of the packet to handle
     * @param handler The packet handler
     */
    public void registerPlayerHandler(Identifier id, PacketHandler<ProxyPlayer> handler) {
        this.playerHandlers.register(id, handler);
    }

    /**
     * Registers a handler for custom packets sent from servers during the play phase
     * @param id The ID of the packet to handle
     * @param handler The packet handler
     */
    public void registerServerHandler(Identifier id, PacketHandler<ServerMessage> handler) {
        this.serverHandlers.register(id, handler);
    }

    /**
     * Registers a handler for custom packets sent from servers during the server's login phase. Unhandled packets will
     * not be sent to players, as they have already left the login phase.
     * @param id The ID of the packet to handle
     * @param handler The packet handler
     */
    public void registerLoginHandler(Identifier id, PacketHandler<ProxyPlayer> handler) {
        this.loginHandlers.register(id, handler);
    }


    /**
     * Unregisters a handler for custom packets sent from clients
     * @param id The ID of the packet to handle
     */
    public void unregisterPlayerHandler(Identifier id) {
        this.playerHandlers.remove(id);
    }

    /**
     * Unregisters a handler for custom packets sent from servers during the play phase
     * @param id The ID of the packet to handle
     */
    public void unregisterServerHandler(Identifier id) {
        this.serverHandlers.remove(id);
    }

    /**
     * Unregisters a handler for custom packets sent from servers during the server's login phase. Unhandled packets will
     * not be sent to players, as they have already left the login phase.
     * @param id The ID of the packet to handle
     */
    public void unregisterLoginHandler(Identifier id) {
        this.loginHandlers.remove(id);
    }


    protected boolean handle(ProxyPlayer player, Identifier id, ByteBuf buffer) {
        return handleGeneric(player, playerHandlers, id, buffer);
    }

    protected void handleLogin(ProxyPlayer player, Identifier id, ByteBuf buffer) {
        handleGeneric(player, loginHandlers, id, buffer);
    }

    protected boolean handleServer(ProxyPlayer player, ProxyServer server, Identifier id, ByteBuf buffer) {
        return handleGeneric(new ServerMessage(player, server), serverHandlers, id, buffer);
    }

    private <T> boolean handleGeneric(T data, Registry<Identifier, PacketHandler<T>> registry, Identifier id, ByteBuf buffer) {

        PacketHandler<T> handler = registry.get(id);
        if(handler == null) {
            return false;
        }

        try {
            handler.handle(data, buffer);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An exception occurred while handling a message from a Server!", ex);
        }

        return true;
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "plugin_message");

    public record ServerMessage(ProxyPlayer player, ProxyServer server) { }

}
