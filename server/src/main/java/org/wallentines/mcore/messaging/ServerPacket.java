package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.mcore.Player;

public interface ServerPacket {

    void write(ByteBuf buffer);

    void handle(Player player);

}
