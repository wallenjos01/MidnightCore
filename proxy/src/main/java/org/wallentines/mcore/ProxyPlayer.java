package org.wallentines.mcore;

import org.wallentines.mcore.text.Component;

import java.util.UUID;

public interface ProxyPlayer {

    UUID getUUID();

    String getUsername();

    void sendMessage(Component message);

    void sendToServer(ProxyServer server);

    ProxyServer getServer();

}
