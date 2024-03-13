package org.wallentines.mcore.messaging;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class FabricClientMessagingModule extends ClientMessagingModule implements ClientPlayNetworking.PlayPayloadHandler<MidnightPayload> {

    @Override
    public void registerPacketHandler(Identifier id, PacketHandler<Client> handler) {
        super.registerPacketHandler(id, handler);

        CustomPacketPayload.Type<MidnightPayload> type = MidnightPayload.type(ConversionUtil.toResourceLocation(id));
        PayloadTypeRegistry.playS2C().register(type, MidnightPayload.codec(type));
        ClientPlayNetworking.registerGlobalReceiver(type, this);
    }

    @Override
    public void registerLoginPacketHandler(Identifier id, ClientLoginPacketHandler handler) {
        super.registerLoginPacketHandler(id, handler);
        ClientLoginNetworking.registerGlobalReceiver(ConversionUtil.toResourceLocation(id), (client, packetListener, buf, responseSender) ->
                CompletableFuture.completedFuture(PacketByteBufs.copy(handleLoginPacket(id, buf))));
    }

    @Override
    public void disable() {
        for (Identifier id : handlers.getIds()) {
            ClientPlayNetworking.unregisterGlobalReceiver(ConversionUtil.toResourceLocation(id));
        }
        for (Identifier id : loginHandlers.getIds()) {
            ClientLoginNetworking.unregisterGlobalReceiver(ConversionUtil.toResourceLocation(id));
        }
    }

    @Override
    public void sendMessage(Packet packet) {

        ((Minecraft) client).submit(() -> {
            FriendlyByteBuf out = PacketByteBufs.create();
            packet.write(out);
            ClientPlayNetworking.send(new MidnightPayload(packet.getId(), out));
        });
    }

    public static final ModuleInfo<Client, ClientModule> MODULE_INFO = new ModuleInfo<>(FabricClientMessagingModule::new, ClientMessagingModule.ID, new ConfigSection());


    @Override
    public void receive(MidnightPayload payload, ClientPlayNetworking.Context context) {

        handlePacket(ConversionUtil.toIdentifier(payload.type().id()), payload.getBuffer());

    }
}
