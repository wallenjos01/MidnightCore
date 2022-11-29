package org.wallentines.midnightcore.api.module.messaging;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.registry.Identifier;

public interface ClientMessagingModule extends Module<MidnightCoreAPI> {

    void registerHandler(Identifier id, ClientMessageHandler handler);

    void registerLoginHandler(Identifier id, ClientMessageHandler handler);

    void sendMessage(Identifier id, ConfigSection data);

    void sendRawMessage(Identifier id, byte[] data);

}
