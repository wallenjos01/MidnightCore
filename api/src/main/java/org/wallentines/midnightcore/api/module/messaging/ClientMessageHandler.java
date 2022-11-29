package org.wallentines.midnightcore.api.module.messaging;

import io.netty.buffer.ByteBuf;

public interface ClientMessageHandler {

    ByteBuf handle(ByteBuf buffer);

}
