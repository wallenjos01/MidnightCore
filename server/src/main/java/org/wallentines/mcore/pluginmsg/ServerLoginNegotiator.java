package org.wallentines.mcore.pluginmsg;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public abstract class ServerLoginNegotiator {

    protected final UUID uuid;
    protected final String name;

    protected ServerLoginNegotiator(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Gets the UUID of the Player who is currently logging in
     * @return The Player's UUID
     */
    public UUID getPlayerUUID() {
        return uuid;
    }

    /**
     * Gets the username of the Player who is currently logging in
     * @return The Player's username
     */
    public String getPlayerName() { return name; }

    /**
     * Sends the given packet to the player, and registers a handler for response
     * @param packet The packet to send
     */
    public void sendPacket(Packet packet) {
        ByteBuf out = Unpooled.buffer();
        packet.write(out);
        sendPacket(packet.getId(), out);
    }

    /**
     * Kicks the player from the server with the given message
     * @param message The kick message
     */
    public abstract void kick(Component message);

    /**
     * Sends a packet with the given ID and data to the player, and registers a handler for response
     * @param id The ID of the packet to send
     * @param data The data to send
     */
    public abstract void sendPacket(Identifier id, ByteBuf data);

}
