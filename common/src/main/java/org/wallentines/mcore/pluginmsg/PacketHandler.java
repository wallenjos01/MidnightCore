package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;

/**
 * A functional interface for handling packets
 * @param <T> The type of additional context available when handling
 */
public interface PacketHandler<T> {

    /**
     * Handles the given buffer
     * @param data Additional data available when handling
     * @param buffer Packet data to parse and handle
     */
    void handle(T data, ByteBuf buffer);

}
