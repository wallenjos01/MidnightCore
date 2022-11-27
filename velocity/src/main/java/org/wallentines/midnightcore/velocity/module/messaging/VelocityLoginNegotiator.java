package org.wallentines.midnightcore.velocity.module.messaging;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.LoginPhaseConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.wallentines.midnightcore.api.module.messaging.LoginMessageHandler;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
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

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(JsonConfigProvider.INSTANCE.saveToString(data));

        sendRawMessage(id, out.toByteArray(), response);
    }

    @Override
    public void sendRawMessage(Identifier id, byte[] data, LoginMessageHandler response) {

        ChannelIdentifier cid = MinecraftChannelIdentifier.create(id.getNamespace(), id.getPath());
        ((LoginPhaseConnection) event.getConnection()).sendLoginPluginMessage(cid, data, msg -> response.handle(new VelocityResponse(msg)));
    }
}
