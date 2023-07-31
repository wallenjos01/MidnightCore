package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;

public interface ClientLoginPacketHandler {

    ByteBuf respond(ByteBuf buffer);

}
