package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

public class FabricServerMessagingModule extends ServerMessagingModule implements ServerPlayNetworking.PlayPayloadHandler<MidnightPayload> {

    @Override
    public boolean initialize(ConfigSection section, Server data) {

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
                onLogin.invoke(new FabricServerLoginNegotiator(handler, sender)));

        return true;
    }

    @Override
    public void sendPacket(Player player, Identifier packetId, ByteBuf data) {

        ServerPlayNetworking.send(ConversionUtil.validate(player), new MidnightPayload(packetId, PacketByteBufs.copy(data)));
    }

    @Override
    public boolean supportsLoginQuery() {
        return true;
    }

    @Override
    protected void doRegister(Identifier packetId) {

        ResourceLocation id = ConversionUtil.toResourceLocation(packetId);
        CustomPacketPayload.Type<MidnightPayload> type = MidnightPayload.type(id);

        if (PayloadTypeRegistryImpl.PLAY_C2S.get(id) == null) {
            PayloadTypeRegistry.playC2S().register(type, MidnightPayload.codec(type));
        }

        ServerPlayNetworking.registerGlobalReceiver(type, this);
    }

    @Override
    protected void doRegisterLogin(Identifier packetId) {
        ServerLoginNetworking.registerGlobalReceiver(ConversionUtil.toResourceLocation(packetId), (server, listener, understood, buf, synchronizer, responseSender) ->
                handleLoginPacket(new FabricServerLoginNegotiator(listener, (LoginPacketSender) responseSender), packetId, buf));
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

    @Override
    public void receive(MidnightPayload payload, ServerPlayNetworking.Context context) {
        handlePacket(context.player(), ConversionUtil.toIdentifier(payload.type().id()), payload.getBuffer());
    }
}
