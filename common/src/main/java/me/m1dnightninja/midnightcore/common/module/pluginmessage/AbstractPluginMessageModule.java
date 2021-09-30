package me.m1dnightninja.midnightcore.common.module.pluginmessage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.pluginmessage.IPluginMessageHandler;
import me.m1dnightninja.midnightcore.api.module.pluginmessage.IPluginMessageModule;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.registry.MRegistry;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;

public abstract class AbstractPluginMessageModule implements IPluginMessageModule {

    public static final MIdentifier ID = MIdentifier.create("midnightcore", "plugin_message");

    protected final MRegistry<IPluginMessageHandler> handlers = new MRegistry<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        return true;
    }

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public ConfigSection getDefaultConfig() {

        return new ConfigSection();
    }

    @Override
    public void registerProvider(MIdentifier id, IPluginMessageHandler handler) {
        handlers.register(id, handler);
    }

    @Override
    public void sendMessage(MPlayer player, MIdentifier id, ConfigSection data) {

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(JsonConfigProvider.INSTANCE.saveToString(data));

        send(player, id, out);
    }

    protected abstract void send(MPlayer player, MIdentifier id, ByteArrayDataOutput data);

    protected void handleRaw(MPlayer sender, ByteArrayDataInput data) {

        MIdentifier id;

        try {
            String channel = data.readUTF();
            id = MIdentifier.parseOrDefault(channel, "midnightcore");

        } catch (Exception ex) {
            // Not for us
            return;
        }

        handle(sender, id, data);
    }

    protected void handle(MPlayer sender, MIdentifier id, ByteArrayDataInput data) {

        IPluginMessageHandler handler = handlers.get(id);
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
