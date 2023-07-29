package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightlib.registry.Identifier;

/**
 * An interface for implementing custom packets
 */
public interface ServerPacket {

    /**
     * Gets the ID of this packet's type
     * @return The packet's type ID
     */
    Identifier getId();

    /**
     * Writes packet data to a buffer
     * @param buffer The buffer to write to
     */
    void write(ByteBuf buffer);

}
