package org.wallentines.midnightcore.velocity.module.messaging;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.LoginPhaseConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.LoginMessageHandler;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
import org.wallentines.midnightcore.common.module.messaging.PacketBufferUtils;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Objects;

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
        PacketBufferUtils.writeUtf(out, JsonConfigProvider.INSTANCE.saveToString(data));

        sendRawMessage(id, out, response);
    }

    @Override
    public void sendRawMessage(Identifier id, ByteBuf data, LoginMessageHandler response) {

        ChannelIdentifier cid = MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());
        ((LoginPhaseConnection) event.getConnection()).sendLoginPluginMessage(cid, data.array(), msg -> {
            response.handle(msg == null ? null : Unpooled.wrappedBuffer(msg));
        });
    }
}
