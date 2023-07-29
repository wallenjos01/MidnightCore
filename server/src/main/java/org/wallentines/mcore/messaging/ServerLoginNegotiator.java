package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
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
     * Sends a packet with the given ID and data to the player, and registers a handler for response
     * @param id The ID of the packet to send
     * @param data The data to send
     * @param response The function to call when the player responds
     */
    public abstract void sendPacket(Identifier id, ByteBuf data, ServerLoginPacketHandler response);

}
