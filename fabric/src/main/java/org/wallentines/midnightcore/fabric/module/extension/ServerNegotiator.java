package org.wallentines.midnightcore.fabric.module.extension;

import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ServerNegotiator {

    private final AtomicInteger transactionId = new AtomicInteger(1000);
    private final ServerLoginPacketListenerImpl packetListener;

    private final HashMap<Integer, TransactionInfo> currentTransactions = new HashMap<>();

    public ServerNegotiator(ServerLoginPacketListenerImpl packetListener) {

        this.packetListener = packetListener;
    }

    public void sendPacket(ResourceLocation id, FriendlyByteBuf data, Consumer<FriendlyByteBuf> response) {

        TransactionInfo inf = new TransactionInfo(packetListener.connection, response);
        int tid = transactionId.getAndIncrement();

        currentTransactions.put(tid, inf);
        packetListener.connection.send(new ClientboundCustomQueryPacket(tid, id, data));
    }

    public void handlePacket(ServerboundCustomQueryPacket packet) {

        TransactionInfo info = currentTransactions.get(packet.getTransactionId());
        if(info != null) {

            info.onResponse.accept(packet.getData());
            currentTransactions.remove(packet.getTransactionId());
        }

    }

    public int itemsInQueue() {
        return currentTransactions.size();
    }

    private static class TransactionInfo {
        Connection conn;
        Consumer<FriendlyByteBuf> onResponse;

        public TransactionInfo(Connection conn, Consumer<FriendlyByteBuf> onResponse) {
            this.conn = conn;
            this.onResponse = onResponse;
        }
    }

}
