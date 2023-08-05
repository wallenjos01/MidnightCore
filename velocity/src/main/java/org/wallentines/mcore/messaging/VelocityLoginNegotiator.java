package org.wallentines.mcore.messaging;

import com.velocitypowered.api.proxy.LoginPhaseConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;

public class VelocityLoginNegotiator extends ProxyLoginNegotiator {

    private final LoginPhaseConnection connection;

    public VelocityLoginNegotiator(String username, LoginPhaseConnection connection) {
        super(username);
        this.connection = connection;
    }

    @Override
    protected void sendMessage(Identifier id, ByteBuf buffer, PacketHandler<ProxyLoginNegotiator> responseHandler) {

        connection.sendLoginPluginMessage(ConversionUtil.toChannelIdentifier(id), buffer.array(), buf -> {
            responseHandler.handle(this, buf == null ? null : Unpooled.wrappedBuffer(buf));
        });

    }
}
