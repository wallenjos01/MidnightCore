package org.wallentines.midnightcore.common.module.messaging;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessageHandler;
import org.wallentines.midnightcore.api.module.messaging.MessageResponse;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.io.*;

public abstract class AbstractMessagingModule implements MessagingModule {

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "messaging");

    protected final Registry<MessageHandler> handlers = new Registry<>();

    @Override
    public boolean initialize(ConfigSection configuration, MidnightCoreAPI api) {

        return true;
    }

    @Override
    public void registerHandler(Identifier id, MessageHandler handler) {
        handlers.register(id, handler);
    }

    @Override
    public void sendMessage(MPlayer player, Identifier id, ConfigSection data) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            out.writeUTF(JsonConfigProvider.INSTANCE.saveToString(data));
            sendRawMessage(player, id, stream.toByteArray());
        } catch (IOException ex) {
            MidnightCoreAPI.getLogger().warn("An error occurred while sending a plugin message!");
        }
    }

    protected void handle(MPlayer sender, Identifier id, MessageResponse res) {

        MessageHandler handler = handlers.get(id);
        if(handler == null) return;

        handler.handle(sender, res);
    }
}
