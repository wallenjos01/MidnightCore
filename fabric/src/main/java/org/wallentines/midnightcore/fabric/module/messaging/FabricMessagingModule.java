package org.wallentines.midnightcore.fabric.module.messaging;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.messaging.AbstractMessagingModule;
import org.wallentines.midnightcore.fabric.event.server.CustomMessageEvent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.*;

public class FabricMessagingModule extends AbstractMessagingModule {

    private FabricMessagingModule() {

        Event.register(CustomMessageEvent.class, this, this::onMessage);
    }

    @Override
    protected void send(MPlayer player, Identifier id, byte[] data) {

        ServerPlayer pl = ((FabricPlayer) player).getInternal();
        if(pl == null) return;

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeUtf(id.toString());
        buf.writeBytes(data);

        pl.connection.send(new ClientboundCustomPayloadPacket(buf));
    }

    private void onMessage(CustomMessageEvent event) {

        MPlayer player = FabricPlayer.wrap(event.getSource());
        FriendlyByteBuf buf = event.getData();

        if(!buf.isReadable() || buf.refCnt() == 0) return;

        DataInput input = new DataInputStream(new ByteArrayInputStream(buf.accessByteBufWithCorrectSize()));
        handleRaw(player, input);
    }

    public static final ModuleInfo<MidnightCoreAPI> MODULE_INFO = new ModuleInfo<>(FabricMessagingModule::new, ID, new ConfigSection());

}
