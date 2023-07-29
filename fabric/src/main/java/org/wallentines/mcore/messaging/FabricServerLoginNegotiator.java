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
import java.util.function.Consumer;

public class FabricServerLoginNegotiator extends ServerLoginNegotiator {

    private int currentTransactionId = 32768;
    private final Connection connection;
    private final HashMap<Integer, Consumer<ByteBuf>> currentTransactions = new HashMap<>();

    public FabricServerLoginNegotiator(UUID player, String name, Connection connection) {
        super(player, name);
        this.connection = connection;
    }

    @Override
    public void sendPacket(Identifier id, ByteBuf data, Consumer<ByteBuf> response) {

        int transactionId = currentTransactionId++;

        currentTransactions.put(transactionId, response);
        connection.send(new ClientboundCustomQueryPacket(transactionId, ConversionUtil.toResourceLocation(id), new FriendlyByteBuf(data)));
    }

    public void handlePacket(int transactionId, FriendlyByteBuf data) {

        Consumer<ByteBuf> handler = currentTransactions.get(transactionId);
        if(handler == null) {
            MidnightCoreAPI.LOGGER.warn("Received unsolicited login query packet from " + getPlayerName() + "!");
            return;
        }

        handler.accept(data);
        currentTransactions.remove(transactionId);
    }

    public int itemsInQueue() {
        return currentTransactions.size();
    }
}
