package org.wallentines.mcore;

import org.wallentines.mcore.lang.LocaleHolder;
import org.wallentines.mcore.lang.PlaceholderManager;
import org.wallentines.mcore.lang.PlaceholderSupplier;
import org.wallentines.mcore.text.Component;

import java.util.UUID;

/**
 * Represents a proxied player
 */
public interface ProxyPlayer extends LocaleHolder, PermissionHolder {

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
    String getLanguage();

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
     * Gets the hostname the player used to connect to the proxy
     * @return The hostname used to connect
     */
    String getHostname();


    /**
     * Registers placeholders for proxied players into the given PlaceholderManager
     * @param manager The PlaceholderManager to register placeholders into.
     */
    static void registerPlaceholders(PlaceholderManager manager) {
        manager.registerSupplier("player_name", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(ProxyPlayer.class, ProxyPlayer::getUsername, "")));
        manager.registerSupplier("player_uuid", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(ProxyPlayer.class, pl -> pl.getUUID().toString(), "")));
        manager.registerSupplier("player_hostname", PlaceholderSupplier.inline(ctx -> ctx.onValueOr(ProxyPlayer.class, ProxyPlayer::getHostname, "")));
    }
}
