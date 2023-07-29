package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.event.CustomPayloadEvent;
import org.wallentines.mcore.event.LoginQueryEvent;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricServerMessagingModule extends ServerMessagingModule {


    @Override
    public void sendPacket(Player player, Identifier packetId, ByteBuf data) {
        ConversionUtil.ensureValid(player).connection.send(
                new ClientboundCustomPayloadPacket(
                        ConversionUtil.toResourceLocation(packetId),
                        new FriendlyByteBuf(data)
                )
        );
    }

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        Event.register(CustomPayloadEvent.class, this, ev -> handlePacket(ev.sender(), ConversionUtil.toIdentifier(ev.packetId()), ev.data()));
        Event.register(LoginQueryEvent.class, this, ev -> onLogin.invoke(ev.negotiator()));

        return true;
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricServerMessagingModule::new, ID, DEFAULT_CONFIG);
}
