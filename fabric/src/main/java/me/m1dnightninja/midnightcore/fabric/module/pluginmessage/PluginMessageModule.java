package me.m1dnightninja.midnightcore.fabric.module.pluginmessage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.common.module.pluginmessage.AbstractPluginMessageModule;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.PluginMessageEvent;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;


public class PluginMessageModule extends AbstractPluginMessageModule {

    @Override
    public boolean initialize(ConfigSection configuration) {

        Event.register(PluginMessageEvent.class, this, this::onMessage);
        return super.initialize(configuration);
    }

    @Override
    protected void send(MPlayer player, MIdentifier id, ByteArrayDataOutput data) {

        ServerPlayer pl = ((FabricPlayer) player).getMinecraftPlayer();
        if(pl == null) return;

        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(id.toString());
        buf.writeBytes(data.toByteArray());

        pl.connection.send(new ClientboundCustomPayloadPacket(buf));
    }

    private void onMessage(PluginMessageEvent event) {

        MPlayer player = FabricPlayer.wrap(event.getSource());
        FriendlyByteBuf buf = event.getData();

        ByteArrayDataInput inp = ByteStreams.newDataInput(buf.accessByteBufWithCorrectSize());
        handleRaw(player, inp);
    }
}
