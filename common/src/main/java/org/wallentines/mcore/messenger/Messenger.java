package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;

public interface Messenger {

    void unsubscribe(String channel);

    void subscribe(String channel, MessageHandler handler);

    void publish(String channel, ByteBuf message);

    void queue(String channel, ByteBuf message);

    default void shutdown() { }

}
