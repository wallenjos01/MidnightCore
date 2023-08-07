package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.*;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.mixin.AccessorLoginPacketHandler;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricServerMessagingModule extends ServerMessagingModule {

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
                onLogin.invoke(new FabricServerLoginNegotiator(((AccessorLoginPacketHandler) handler).getGameProfile(), sender)));

        return true;
    }

    @Override
    public void sendPacket(Player player, Identifier packetId, ByteBuf data) {

        ServerPlayNetworking.send(ConversionUtil.validate(player), ConversionUtil.toResourceLocation(packetId), PacketByteBufs.copy(data));
    }

    @Override
    public boolean supportsLoginQuery() {
        return true;
    }

    @Override
    protected void doRegister(Identifier packetId) {
        ServerPlayNetworking.registerGlobalReceiver(ConversionUtil.toResourceLocation(packetId), (server, player, listener, buf, responseSender) ->
                handlePacket(player, packetId, buf));
    }

    @Override
    protected void doRegisterLogin(Identifier packetId) {
        ServerLoginNetworking.registerGlobalReceiver(ConversionUtil.toResourceLocation(packetId), (server, listener, understood, buf, synchronizer, responseSender) ->
                handleLoginPacket(new FabricServerLoginNegotiator(((AccessorLoginPacketHandler) listener).getGameProfile(), responseSender), packetId, buf));
    }

    @Override
    protected void doUnregister(Identifier packetId) {
        ServerPlayNetworking.unregisterGlobalReceiver(ConversionUtil.toResourceLocation(packetId));
    }

    @Override
    protected void doUnregisterLogin(Identifier packetId) {
        ServerLoginNetworking.unregisterGlobalReceiver(ConversionUtil.toResourceLocation(packetId));
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricServerMessagingModule::new, ID, DEFAULT_CONFIG);
}
