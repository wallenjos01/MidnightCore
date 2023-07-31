package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightlib.registry.Identifier;

/**
 * A custom packet sent to by clients
 */
public interface ClientPacket {

    /**
     * Gets the packet's ID
     * @return the packet ID
     */
    Identifier getId();

    /**
     * Writes the packet data to the given buffer
     * @param buffer The buffer to write to
     */
    void write(ByteBuf buffer);

}
