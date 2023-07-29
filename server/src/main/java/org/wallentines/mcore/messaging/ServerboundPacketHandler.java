package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.Player;

public interface ServerboundPacketHandler {

    void handle(Player sender, ByteBuf packet);

}
