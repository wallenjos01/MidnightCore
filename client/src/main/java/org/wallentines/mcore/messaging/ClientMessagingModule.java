package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

/**
 * A module for sending custom packets to servers and handling custom packets sent from servers.
 */
public abstract class ClientMessagingModule implements ClientModule {

    private final Registry<PacketHandler<Client>> handlers = new Registry<>(MidnightCoreAPI.MOD_ID);
    private final Registry<ClientLoginPacketHandler> loginHandlers = new Registry<>(MidnightCoreAPI.MOD_ID);

    protected Client client;

    @Override
    public boolean initialize(ConfigSection section, Client data) {
        this.client = data;
        return true;
    }

    /**
     * Sends a custom packet to the server
     * @param packet The packet to send
     */
    public abstract void sendMessage(Packet packet);

    /**
     * Registers a packet handler for packets with the given ID send during the play state
     * @param id The packet ID
     * @param handler The packet handler
     */
    public void registerPacketHandler(Identifier id, PacketHandler<Client> handler) {
        handlers.register(id, handler);
    }

    /**
     * Registers a packet handler for packets with the given ID send during the login state
     * @param id The packet ID
     * @param handler The packet handler
     */
    public void registerLoginPacketHandler(Identifier id, ClientLoginPacketHandler handler) {
        loginHandlers.register(id, handler);
    }


    protected boolean handlePacket(Identifier id, ByteBuf buf) {

        PacketHandler<Client> handler = handlers.get(id);
        if(handler == null) {
            return false;
        }

        try {
            handler.handle(client, buf);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.warn("An error occurred while handling a custom packet!");
            ex.printStackTrace();
        }
        return true;
    }


    protected ByteBuf handleLoginPacket(Identifier id, ByteBuf buf) {

        ClientLoginPacketHandler handler = loginHandlers.get(id);
        if(handler == null) {
            return null;
        }

        ByteBuf out = null;
        try {
            out = handler.respond(buf);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.warn("An error occurred while handling a login packet!");
            ex.printStackTrace();
        }

        return out;
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "messaging");

}
