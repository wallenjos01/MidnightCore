package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightlib.registry.Identifier;

public abstract class ProxyLoginNegotiator {

    private final String username;

    public ProxyLoginNegotiator(String username) {
        this.username = username;
    }

    public void sendMessage(Packet packet, PacketHandler<ProxyLoginNegotiator> responseHandler) {

        ByteBuf out = Unpooled.buffer();
        packet.write(out);

        sendMessage(packet.getId(), out, responseHandler);
    }

    public String getUsername() {
        return username;
    }

    protected abstract void sendMessage(Identifier id, ByteBuf buffer, PacketHandler<ProxyLoginNegotiator> responseHandler);
}
