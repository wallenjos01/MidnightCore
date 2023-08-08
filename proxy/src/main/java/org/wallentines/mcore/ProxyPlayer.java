package org.wallentines.mcore;

import org.wallentines.mcore.text.Component;

import java.util.UUID;

/**
 * Represents a proxied player
 */
public interface ProxyPlayer {

    /**
     * Gets the UUID of the player
     * @return The player's UUID
     */
    UUID getUUID();

    /**
     * Gets the username of the player
     * @return The player's username
     */
    String getUsername();

    /**
     * Gets the proxy instance which created the player object
     * @return The proxy
     */
    Proxy getProxy();

    /**
     * Gets the locale for a player
     * @return The player's locale
     */
    String getLocale();

    /**
     * Sends a component message to the player
     * @param message The message to send
     */
    void sendMessage(Component message);

    /**
     * Sends the player to another server
     * @param server The server to connect the player to
     */
    void sendToServer(ProxyServer server);

    /**
     * Gets the server the player is currently on. This may be null while they are still logging in
     * @return The player's server
     */
    ProxyServer getServer();

    /**
     * Checks if the player has the given permission
     * @param permission The permission to check
     * @return Whether the player has the permission
     */
    boolean hasPermission(String permission);
}
