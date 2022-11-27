package org.wallentines.midnightcore.common.module.messaging;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractMessagingModule implements MessagingModule {

    protected List<Consumer<LoginNegotiator>> loginHandlers = new ArrayList<>();

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

    @Override
    public void addLoginListener(Consumer<LoginNegotiator> onLogin) {

        loginHandlers.add(onLogin);
    }

    protected void handle(MPlayer sender, Identifier id, MessageResponse res) {

        MessageHandler handler = handlers.get(id);
        if(handler == null) return;

        handler.handle(sender, res);
    }

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    public static int readVarInt(DataInput input) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            try {
                currentByte = input.readByte();
                value |= (currentByte & SEGMENT_BITS) << position;
                if ((currentByte & CONTINUE_BIT) == 0) break;
                position += 7;
                if (position >= 32) throw new RuntimeException("VarInt is too big");
            } catch (IOException ex) {
                // Ignore
            }
        }

        return value;
    }

    public static void writeVarInt(DataOutput output, int value) {
        while (true) {

            try {
                if ((value & ~SEGMENT_BITS) == 0) {
                    output.writeByte(value);
                    return;
                }

                output.writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);

                // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
                value >>>= 7;
            } catch (IOException ex) {
                // Ignore
            }
        }
    }

}
