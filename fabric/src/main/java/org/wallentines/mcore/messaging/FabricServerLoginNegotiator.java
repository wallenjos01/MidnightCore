package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class FabricServerLoginNegotiator extends ServerLoginNegotiator {

    private int currentTransactionId = 32768;
    private final Connection connection;
    private final HashMap<Integer, ServerLoginPacketHandler> currentTransactions = new HashMap<>();

    /**
     * Creates a new login negotiator for the player with the given UUID, username, and Connection
     * @param uuid The UUID of the logging in player
     * @param name The username of the logging in player
     * @param connection The connection to the logging in player
     */
    public FabricServerLoginNegotiator(UUID uuid, String name, Connection connection) {
        super(uuid, name);
        this.connection = connection;
    }

    @Override
    public void sendPacket(Identifier id, ByteBuf data, ServerLoginPacketHandler response) {

        int transactionId = currentTransactionId++;

        currentTransactions.put(transactionId, response);
        connection.send(new ClientboundCustomQueryPacket(transactionId, ConversionUtil.toResourceLocation(id), new FriendlyByteBuf(data)));
    }

    /**
     * Handles a packet received from the player with the given transaction id and data
     * @param transactionId The transaction id of the received packet
     * @param data The data of the recieved packet
     */
    public void handlePacket(int transactionId, FriendlyByteBuf data) {

        ServerLoginPacketHandler handler = currentTransactions.get(transactionId);
        if(handler == null) {
            MidnightCoreAPI.LOGGER.warn("Received unsolicited login query packet from " + name + "!");
            return;
        }

        handler.handle(uuid, name, data);
        currentTransactions.remove(transactionId);
    }

    /**
     * Determines the number of packets yet to be handled. The connection should transition out of negotiation when this
     * reaches zero
     * @return The number of packets yet to be handled.
     */
    public int itemsInQueue() {
        return currentTransactions.size();
    }
}
