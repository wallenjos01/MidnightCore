package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import org.wallentines.mcore.*;
import org.wallentines.mcore.event.ClientCustomPayloadEvent;
import org.wallentines.mcore.event.ClientLoginQueryEvent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;

@Environment(EnvType.CLIENT)
public class FabricClientMessagingModule extends ClientMessagingModule {

    @Override
    public boolean initialize(ConfigSection section, Client data) {

        Event.register(ClientCustomPayloadEvent.class, this, 30, ev -> {
            if(ev.isHandled()) return;
            if(handlePacket(ConversionUtil.toIdentifier(ev.getPacketId()), ev.getData())) {
                ev.setHandled(true);
            }
        });
        Event.register(ClientLoginQueryEvent.class, this, ev -> {
            ByteBuf response = handleLoginPacket(ConversionUtil.toIdentifier(ev.getPacketId()), ev.getData());
            if(response != null) {
                response.resetReaderIndex();
                ev.setResponse(new FriendlyByteBuf(response));
            }
        });

        return true;
    }

    @Override
    public void sendMessage(ClientPacket packet) {

        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if(listener == null) {
            MidnightCoreAPI.LOGGER.warn("Attempt to send packet before connecting to a server!");
            return;
        }

        ByteBuf out = Unpooled.buffer();
        packet.write(out);
        out.resetReaderIndex();

        listener.send(new ServerboundCustomPayloadPacket(ConversionUtil.toResourceLocation(packet.getId()), new FriendlyByteBuf(out)));
    }

    public static final ModuleInfo<Client, ClientModule> MODULE_INFO = new ModuleInfo<>(FabricClientMessagingModule::new, ClientMessagingModule.ID, new ConfigSection());
}
