package org.wallentines.midnightcore.common.module.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.LoginNegotiator;
import org.wallentines.midnightcore.api.module.messaging.MessageHandler;
import org.wallentines.midnightcore.api.module.messaging.MessagingModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.midnightlib.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractMessagingModule implements MessagingModule {

    protected List<Consumer<LoginNegotiator>> loginHandlers = new ArrayList<>();

    public static final Identifier ID = new Identifier(MidnightCoreAPI.DEFAULT_NAMESPACE, "messaging");

    protected final Registry<MessageHandler> handlers = new Registry<>(MidnightCoreAPI.DEFAULT_NAMESPACE);

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
        PacketBufferUtils.writeUtf(buf, JSONCodec.minified().encodeToString(ConfigContext.INSTANCE, data));

        sendRawMessage(player, id, buf.array());
    }

    @Override
    public void addLoginListener(Consumer<LoginNegotiator> onLogin) {

        loginHandlers.add(onLogin);
    }

    @Override
    public void unregisterHandler(Identifier id) {

        handlers.remove(id);
    }

    protected void handle(MPlayer sender, Identifier id, byte[] data) {
        handle(sender, id, Unpooled.wrappedBuffer(data));
    }

    protected void handle(MPlayer sender, Identifier id, ByteBuf res) {

        MessageHandler handler = handlers.get(id);
        if(handler == null) return;

        handler.handle(sender, res);
    }

    @Override
    public void disable() {

        loginHandlers.clear();
        handlers.clear();

        MessagingModule.super.disable();
    }
}
