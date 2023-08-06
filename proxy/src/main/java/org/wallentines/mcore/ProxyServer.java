package org.wallentines.mcore;

/**
 * Represents a backend server registered on the proxy
 */
public interface ProxyServer {

    /**
     * Gets the IP or hostname of the server
     * @return The server's address
     */
    String getAddress();

    /**
     * Gets the port on which the proxied server is running
     * @return The server's port
     */
    String getPort();

    /**
     * Gets the server's registered name
     * @return The server's name
     */
    String getName();

}
