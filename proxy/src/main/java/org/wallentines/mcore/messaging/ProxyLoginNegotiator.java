package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightlib.registry.Identifier;

/**
 * An object which handles sending custom packets during the Player's login phase. Note that the player will only be in
 * the login phase once: when they are first connecting to the proxy. A player will not re-enter the login phase when
 * changing servers.
 */
public abstract class ProxyLoginNegotiator {

    private final String username;

    protected ProxyLoginNegotiator(String username) {
        this.username = username;
    }

    /**
     * Sends a message to the client and registers a handler for the player's response
     * @param packet The packet to send
     * @param responseHandler The handler to run when the player responds
     */
    public void sendMessage(Packet packet, PacketHandler<ProxyLoginNegotiator> responseHandler) {

        ByteBuf out = Unpooled.buffer();
        packet.write(out);

        sendMessage(packet.getId(), out, responseHandler);
    }

    /**
     * The player's username. Although UUIDs should be available at this stage, the Velocity API does not expose them.
     * @return The username of the player who is logging in.
     */
    public String getUsername() {
        return username;
    }

    protected abstract void sendMessage(Identifier id, ByteBuf buffer, PacketHandler<ProxyLoginNegotiator> responseHandler);
}
