package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightlib.registry.Identifier;

/**
 * An interface representing a packet which can be sent over the network
 */
public interface Packet {

    /**
     * The packet's type ID
     * @return The ID
     */
    Identifier getId();

    /**
     * Writes the packet to a buffer
     * @param buffer The buffer to write to
     */
    void write(ByteBuf buffer);

}
