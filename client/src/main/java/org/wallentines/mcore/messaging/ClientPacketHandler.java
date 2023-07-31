package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;

public interface ClientPacketHandler {

    void handle(ByteBuf buf);

}
