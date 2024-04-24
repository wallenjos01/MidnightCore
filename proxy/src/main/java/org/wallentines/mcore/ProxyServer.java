package org.wallentines.mcore;

import org.wallentines.midnightlib.event.HandlerList;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Represents a backend server registered on the proxy
 */
public interface ProxyServer {

    /**
     * Gets the server's socket address, including the hostname/IP and port.
     * @return The server's address
     */
    InetSocketAddress getAddress();

    /**
     * Gets the server's registered name
     * @return The server's name
     */
    String getName();

    /**
     * Gets the proxy instance which created the server object
     * @return The proxy
     */
    Proxy getProxy();

    /**
     * Gets a list of players connected to this server
     * @return A list of online players
     */
    Collection<ProxyPlayer> getPlayers();


    /**
     * Gets an event called when a player connects
     * @return The server's connect event
     */
    HandlerList<ProxyPlayer> connectEvent();

}
