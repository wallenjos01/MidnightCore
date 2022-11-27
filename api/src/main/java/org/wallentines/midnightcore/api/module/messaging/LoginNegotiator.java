package org.wallentines.midnightcore.api.module.messaging;

import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public interface LoginNegotiator {

    String getPlayerUsername();

    void sendMessage(Identifier id, ConfigSection data, LoginMessageHandler response);

    void sendRawMessage(Identifier id, byte[] data, LoginMessageHandler response);

}
