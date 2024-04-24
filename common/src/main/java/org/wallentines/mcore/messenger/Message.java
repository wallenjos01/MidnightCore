package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;

import java.util.Objects;

/**
 * Represents a message received by a messenger module
 */
public final class Message {

    private final Messenger parent;
    private final String channel;
    private final ByteBuf payload;

    public Message(Messenger parent, String channel, ByteBuf payload) {
        this.parent = parent;
        this.channel = channel;
        this.payload = payload;
    }

    /**
     * Gets the messenger which received this message
     * @return The receiving messenger
     */
    public Messenger parent() {
        return parent;
    }

    /**
     * Gets the channel this message was sent in
     * @return The message channel
     */
    public String channel() {
        return channel;
    }

    /**
     * Gets the message's contents
     * @return The message content
     */
    public ByteBuf payload() {
        return payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(parent, message.parent) && Objects.equals(channel, message.channel) && Objects.equals(payload, message.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, channel, payload);
    }

    @Override
    public String toString() {
        return "Message{" +
                "parent=" + parent +
                ", channel='" + channel + '\'' +
                '}';
    }
}
