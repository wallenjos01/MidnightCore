package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;

/**
 * Handles a custom packet sent during the play phase
 */
public interface ClientPacketHandler {

    /**
     * Handles a custom packet
     * @param buf The received packet
     */
    void handle(ByteBuf buf);

}
