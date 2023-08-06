package org.wallentines.mcore;

import java.net.InetSocketAddress;

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

}
