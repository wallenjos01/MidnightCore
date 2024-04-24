package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightlib.event.EventHandler;

/**
 * An interface for implementations used by the messaging module to send messages to other servers.
 */
public interface Messenger {


    /**
     * Registers the given handler to handle messages in the given channel
     * @param listener A listener
     * @param channel The channel to listen to
     * @param handler The handler for messages in that channel
     */
    void subscribe(Object listener, String channel, EventHandler<Message> handler);

    /**
     * Unregisters all handlers for the given listener in the given channel
     * @param listener A listener
     * @param channel The channel to stop listening to
     */
    void unsubscribe(Object listener, String channel);

    /**
     * Sends a message with the given bytes to the given channel
     * @param channel The channel to publish to
     * @param message The message's data
     */
    void publish(String channel, ByteBuf message);

    /**
     * Sends a message with the given bytes to the given channel, or queues it if it cannot be sent immediately
     * @param channel The channel to publish to
     * @param message The message's data
     */
    void queue(String channel, ByteBuf message);

    /**
     * Cleans up the messenger. Only to be called internally
     */
    default void shutdown() { }

}
