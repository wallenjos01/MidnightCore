package org.wallentines.mcore.messaging;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.wallentines.mcore.Client;
import org.wallentines.mcore.ClientModule;
import org.wallentines.mcore.util.ConversionUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.module.ModuleInfo;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public class FabricClientMessagingModule extends ClientMessagingModule {

    @Override
    public void registerPacketHandler(Identifier id, PacketHandler<Client> handler) {
        super.registerPacketHandler(id, handler);
        ClientPlayNetworking.registerGlobalReceiver(ConversionUtil.toResourceLocation(id), (client, packetListener, buf, responseSender) ->
                handlePacket(id, buf));
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
            ClientPlayNetworking.send(ConversionUtil.toResourceLocation(packet.getId()), out);
        });
    }

    public static final ModuleInfo<Client, ClientModule> MODULE_INFO = new ModuleInfo<>(FabricClientMessagingModule::new, ClientMessagingModule.ID, new ConfigSection());
}
