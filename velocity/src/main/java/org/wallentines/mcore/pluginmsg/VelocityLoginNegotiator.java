package org.wallentines.mcore.pluginmsg;

import com.velocitypowered.api.proxy.LoginPhaseConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;

public class VelocityLoginNegotiator extends ProxyLoginNegotiator {

    private final LoginPhaseConnection connection;

    /**
     * Constructs a new login negotiator with the given username and connection
     * @param username The username the player logged in with
     * @param connection The player's connection
     */
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
