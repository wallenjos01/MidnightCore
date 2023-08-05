package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;

public interface PacketHandler<T> {

    void handle(T data, ByteBuf buffer);

}
