package org.wallentines.midnightcore.velocity.module.messaging;

import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.LoginPhaseConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.module.messaging.LoginMessageHandler;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
import org.wallentines.midnightcore.common.module.messaging.PacketBufferUtils;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public class VelocityLoginNegotiator implements LoginNegotiator {

    private final PreLoginEvent event;

    public VelocityLoginNegotiator(PreLoginEvent event) {
        this.event = event;
    }

    @Override
    public String getPlayerUsername() {
        return event.getUsername();
    }

    @Override
    public void sendMessage(Identifier id, ConfigSection data, LoginMessageHandler response) {

        ByteBuf out = Unpooled.buffer();
        PacketBufferUtils.writeUtf(out, JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, data));

        sendRawMessage(id, out, response);
    }

    @Override
    public void sendRawMessage(Identifier id, ByteBuf data, LoginMessageHandler response) {

        ChannelIdentifier cid = MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());
        ((LoginPhaseConnection) event.getConnection()).sendLoginPluginMessage(cid, data.array(), msg ->
                response.handle(msg == null ? null : Unpooled.wrappedBuffer(msg)));
    }
}
