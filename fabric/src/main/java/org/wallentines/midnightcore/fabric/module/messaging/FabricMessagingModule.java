package org.wallentines.midnightcore.fabric.module.messaging;

import io.netty.buffer.Unpooled;
import io.netty.util.IllegalReferenceCountException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.fabric.event.server.CustomMessageEvent;
import org.wallentines.midnightcore.fabric.event.server.ServerBeginQueryEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;


public class FabricMessagingModule extends AbstractMessagingModule {


    private FabricMessagingModule() {

        Event.register(CustomMessageEvent.class, this, this::onMessage);
    }

    @Override
    public boolean initialize(ConfigSection configuration, MServer server) {

        Event.register(ServerBeginQueryEvent.class, this, ev ->
                loginHandlers.forEach(l -> l.accept(ev.getNegotiator())));

        return super.initialize(configuration, server);
    }

    @Override
    public void sendRawMessage(MPlayer player, Identifier id, byte[] data) {

        ServerPlayer pl = ((FabricPlayer) player).getInternal();
        if(pl == null) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(data == null ? Unpooled.buffer() : Unpooled.wrappedBuffer(data));
        pl.connection.send(new ClientboundCustomPayloadPacket(ConversionUtil.toResourceLocation(id), buf));
    }

    private void onMessage(CustomMessageEvent event) {

        if(event.isHandled()) return;

        MPlayer player = FabricPlayer.wrap(event.getSource());
        FriendlyByteBuf buf = event.getData();

        try {
            if (!buf.isReadable()) return;

            Identifier id = ConversionUtil.toIdentifier(event.getPacketId());
            if(!handlers.contains(id)) return;

            try {
                handle(player, id, buf);
            } catch (Exception ex) {
                MidnightCoreAPI.getLogger().warn("An exception occurred while processing a plugin message!");
                ex.printStackTrace();
            }

            event.setHandled(true);

        } catch (IllegalReferenceCountException ex) {
            // Ignore
        }
    }

    public static final ModuleInfo<MServer, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricMessagingModule::new, ID, new ConfigSection());

}
