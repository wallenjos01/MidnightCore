package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;

public interface MessageHandler {

    void handle(Messenger messenger, String channel, ByteBuf data);

}
