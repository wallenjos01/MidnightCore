package org.wallentines.midnightcore.common.module.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.MessageHandler;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

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

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(JsonConfigProvider.INSTANCE.saveToString(data));

        send(player, id, out);
    }

    protected abstract void send(MPlayer player, Identifier id, ByteArrayDataOutput data);

    protected void handleRaw(MPlayer sender, ByteArrayDataInput data) {

        Identifier id;

        try {
            String channel = data.readUTF();

            if(channel.isEmpty()) return;
            id = Identifier.parseOrDefault(channel, "midnightcore");

        } catch (Exception ex) {
            // Not for us
            return;
        }

        handle(sender, id, data);
    }

    protected void handle(MPlayer sender, Identifier id, ByteArrayDataInput data) {

        MessageHandler handler = handlers.get(id);
        if(handler == null) return;

        try {

            ConfigSection sec = JsonConfigProvider.INSTANCE.loadFromString(data.readUTF());
            if (sec == null) return;

            handler.handle(sender, sec);

        } catch (Exception ex) {
            MidnightCoreAPI.getLogger().warn("Unable to parse plugin message!");
            ex.printStackTrace();
        }
    }
}
