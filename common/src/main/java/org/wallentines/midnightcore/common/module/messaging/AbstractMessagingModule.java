package org.wallentines.midnightcore.common.module.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
import org.wallentines.midnightcore.api.module.messaging.MessageHandler;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractMessagingModule implements MessagingModule {

    protected List<Consumer<LoginNegotiator>> loginHandlers = new ArrayList<>();

    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "messaging");

    protected final Registry<MessageHandler> handlers = new Registry<>();

    @Override
    public boolean initialize(ConfigSection configuration, MServer server) {

        return true;
    }

    @Override
    public void registerHandler(Identifier id, MessageHandler handler) {
        handlers.register(id, handler);
    }

    @Override
    public void sendMessage(MPlayer player, Identifier id, ConfigSection data) {

        ByteBuf buf = Unpooled.buffer();
        PacketBufferUtils.writeUtf(buf, JsonConfigProvider.INSTANCE.saveToString(data));

        sendRawMessage(player, id, buf.array());
    }

    @Override
    public void addLoginListener(Consumer<LoginNegotiator> onLogin) {

        loginHandlers.add(onLogin);
    }

    protected void handle(MPlayer sender, Identifier id, ByteBuf res) {

        MessageHandler handler = handlers.get(id);
        if(handler == null) return;

        handler.handle(sender, res);
    }


}
