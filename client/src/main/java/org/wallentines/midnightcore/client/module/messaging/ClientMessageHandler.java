package org.wallentines.midnightcore.client.module.messaging;

import io.netty.buffer.ByteBuf;

public interface ClientMessageHandler {

    ByteBuf handle(ByteBuf buffer);

}
