package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;

/**
 * Handles a custom packet sent during the login phase
 */
public interface ClientLoginPacketHandler {

    /**
     * Handles a login packet
     * @param buffer The received packet
     * @return The response packet
     */
    ByteBuf respond(ByteBuf buffer);

}
