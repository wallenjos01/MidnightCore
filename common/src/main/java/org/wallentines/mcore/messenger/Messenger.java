package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightlib.event.EventHandler;

import java.nio.charset.StandardCharsets;

/**
 * An interface for implementations used by the messaging module to send messages to other servers.
 */
public abstract class Messenger {


    protected final MessengerModule module;
    private boolean isShutdown;

    protected Messenger(MessengerModule module) {
        this.module = module;
    }

    /**
     * Registers the given handler to handle messages in the given channel
     * @param listener A listener
     * @param channel The channel to listen to
     * @param handler The handler for messages in that channel
     */
    public abstract void subscribe(Object listener, String channel, EventHandler<Message> handler);

    /**
     * Unregisters all handlers for the given listener in the given channel
     * @param listener A listener
     * @param channel The channel to stop listening to
     */
    public abstract void unsubscribe(Object listener, String channel);

    /**
     * Sends a message with the given bytes to the given channel
     * @param channel The channel to publish to
     * @param message The message's data
     */
    public abstract void publish(String channel, ByteBuf message);

    /**
     * Sends a message with the given bytes to the given channel
     * @param channel The channel to publish to
     * @param message The message's data
     */
    public void publish(String channel, String message) {
        publish(channel, Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Sends a message with the given bytes to the given channel
     * @param channel The channel to publish to
     * @param ttl how long the message should live before being considered expired
     * @param message The message's data
     */
    public abstract void publish(String channel, int ttl, ByteBuf message);

    /**
     * Sends a message with the given bytes to the given channel
     * @param channel The channel to publish to
     * @param ttl how long the message should live before being considered expired
     * @param message The message's data
     */
    public  void publish(String channel, int ttl, String message) {
        publish(channel, ttl, Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Gets the module which created this messenger
     * @return The messenger module
     */
    public MessengerModule getModule() {
        return module;
    }

    /**
     * Determines if the messenger was shutdown
     * @return Whether the messenger was shutdown
     */
    public boolean isShutdown() {
        return isShutdown;
    }

    /**
     * Cleans up the messenger. Only to be called internally
     */
    public final void shutdown() {

        isShutdown = true;
        onShutdown();
    }

    protected void onShutdown() { }

}
