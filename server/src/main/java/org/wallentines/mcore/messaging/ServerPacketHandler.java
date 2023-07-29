package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.Player;

/**
 * A functional interface for handling custom packets received from clients
 */
public interface ServerPacketHandler {

    void handle(Player player, ByteBuf data);

}
