package org.wallentines.midnightcore.fabric.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.messaging.ClientMessageHandler;
import org.wallentines.midnightcore.api.module.messaging.ClientMessagingModule;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.fabric.event.client.ClientCustomMessageEvent;
import org.wallentines.midnightcore.fabric.event.client.ClientLoginQueryEvent;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.Module;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;

public class FabricClientMessagingModule implements ClientMessagingModule {

    private final HashMap<ResourceLocation, ClientMessageHandler> handlers = new HashMap<>();
    private final HashMap<ResourceLocation, ClientMessageHandler> loginHandlers = new HashMap<>();

    @Override
    public boolean initialize(ConfigSection section, MidnightCoreAPI data) {

        Event.register(ClientCustomMessageEvent.class, this, ev -> {

            if(ev.isHandled()) return;

            ClientMessageHandler handler = handlers.get(ev.getId());
            if(handler == null) return;

            try {
                ByteBuf buf = handler.handle(ev.getData());
                if(buf != null) {
                    ev.respond(buf instanceof FriendlyByteBuf ? (FriendlyByteBuf) buf : new FriendlyByteBuf(buf));
                }
                ev.setHandled(true);

            } catch (Exception ex) {
                MidnightCoreAPI.getLogger().warn("An error occurred while handling a plugin message!");
                ex.printStackTrace();
            }

        });

        Event.register(ClientLoginQueryEvent.class, this, 90, ev -> {

            if(ev.hasResponded()) return;

            ClientMessageHandler handler = loginHandlers.get(ev.getId());
            if(handler == null) return;

            ByteBuf buf = null;
            try {
                buf = handler.handle(ev.getData());

            } catch (Exception ex) {
                MidnightCoreAPI.getLogger().warn("An error occurred while handling a login query!");
                ex.printStackTrace();
            }

            if(buf == null) buf = new FriendlyByteBuf(Unpooled.buffer());
            ev.respond(buf instanceof FriendlyByteBuf ? (FriendlyByteBuf) buf : new FriendlyByteBuf(buf));

        });

        return true;
    }

    @Override
    public void registerHandler(Identifier id, ClientMessageHandler handler) {

        handlers.put(ConversionUtil.toResourceLocation(id), handler);
    }

    @Override
    public void registerLoginHandler(Identifier id, ClientMessageHandler handler) {

        loginHandlers.put(ConversionUtil.toResourceLocation(id), handler);
    }

    @Override
    public void sendMessage(Identifier id, ConfigSection data) {

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(JsonConfigProvider.INSTANCE.saveToString(data));

        sendRawMessage(id, buf.array());
    }

    @Override
    public void sendRawMessage(Identifier id, byte[] data) {

        FriendlyByteBuf buf = new FriendlyByteBuf(data == null ? Unpooled.buffer() : Unpooled.wrappedBuffer(data));
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if(listener == null) {
            throw new IllegalStateException("Attempt to send a plugin message while not connected to a server!");
        }

        listener.send(new ServerboundCustomPayloadPacket(ConversionUtil.toResourceLocation(id), buf));

    }

    public static final ModuleInfo<MidnightCoreAPI, Module<MidnightCoreAPI>> MODULE_INFO = new ModuleInfo<>(FabricClientMessagingModule::new, AbstractMessagingModule.ID, new ConfigSection());

}
