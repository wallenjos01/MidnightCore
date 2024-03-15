package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.mcore.ConfiguringPlayer;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.Server;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FabricServerMessagingModule extends ServerMessagingModule implements ServerPlayNetworking.PlayPayloadHandler<MidnightPayload>, ServerConfigurationNetworking.ConfigurationPacketHandler<MidnightPayload> {

    private static final Map<Identifier, CustomPacketPayload.Type<MidnightPayload>> TYPES = new HashMap<>();

    private static CustomPacketPayload.Type<MidnightPayload> getPayloadType(Identifier id) {
        return TYPES.computeIfAbsent(id, key -> {
            CustomPacketPayload.Type<MidnightPayload> type = MidnightPayload.type(ConversionUtil.toResourceLocation(id));
            PayloadTypeRegistry.playC2S().register(type, MidnightPayload.codec(type));
            return type;
        });
    }

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
    protected void sendPacket(ConfiguringPlayer player, Identifier packetId, ByteBuf data) {

        ServerConfigurationNetworking.send(ConversionUtil.validate(player), new MidnightPayload(packetId, PacketByteBufs.copy(data)));
    }

    @Override
    public boolean supportsLoginQuery() {
        return true;
    }

    @Override
    public boolean supportsConfigMessaging() {
        return true;
    }

    @Override
    protected void doRegister(Identifier packetId) {
        ServerPlayNetworking.registerGlobalReceiver(getPayloadType(packetId), this);
    }

    @Override
    protected void doRegisterLogin(Identifier packetId) {
        ServerLoginNetworking.registerGlobalReceiver(ConversionUtil.toResourceLocation(packetId), (server, listener, understood, buf, synchronizer, responseSender) ->
                handleLoginPacket(new FabricServerLoginNegotiator(listener, (LoginPacketSender) responseSender), packetId, buf));
    }

    @Override
    protected void doRegisterConfig(Identifier packetId) {
        ServerConfigurationNetworking.registerGlobalReceiver(getPayloadType(packetId), this);
    }

    @Override
    protected void doUnregister(Identifier packetId) {

        ServerPlayNetworking.unregisterGlobalReceiver(ConversionUtil.toResourceLocation(packetId));
    }

    @Override
    protected void doUnregisterLogin(Identifier packetId) {
        ServerLoginNetworking.unregisterGlobalReceiver(ConversionUtil.toResourceLocation(packetId));
    }

    @Override
    protected void doUnregisterConfig(Identifier packetId) {
        ServerConfigurationNetworking.unregisterGlobalReceiver(ConversionUtil.toResourceLocation(packetId));
    }

    @Override
    public void receive(MidnightPayload payload, ServerPlayNetworking.Context context) {
        handlePacket(context.player(), ConversionUtil.toIdentifier(payload.type().id()), payload.getBuffer());
    }

    @Override
    public void receive(MidnightPayload payload, ServerConfigurationNetworking.Context context) {
        handleConfigPacket((ConfiguringPlayer) context.networkHandler(), ConversionUtil.toIdentifier(payload.type().id()), payload.getBuffer());
    }

    public static final ModuleInfo<Server, ServerModule> MODULE_INFO = new ModuleInfo<>(FabricServerMessagingModule::new, ID, new ConfigSection());

}
