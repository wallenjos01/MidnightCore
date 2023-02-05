package org.wallentines.midnightcore.api.module.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public interface LoginNegotiator {

    String getPlayerUsername();

    void sendMessage(Identifier id, ConfigSection data, LoginMessageHandler response);

    void sendRawMessage(Identifier id, ByteBuf data, LoginMessageHandler response);

}
