package org.wallentines.midnightcore.client.module.messaging;

import org.wallentines.midnightcore.client.module.ClientModule;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

@SuppressWarnings("unused")
public interface ClientMessagingModule extends ClientModule {

    void registerHandler(Identifier id, ClientMessageHandler handler);

    void registerLoginHandler(Identifier id, ClientMessageHandler handler);

    void sendMessage(Identifier id, ConfigSection data);

    void sendRawMessage(Identifier id, byte[] data);

}
