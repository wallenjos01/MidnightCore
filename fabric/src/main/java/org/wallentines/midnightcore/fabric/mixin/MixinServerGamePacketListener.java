package org.wallentines.midnightcore.fabric.mixin;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.fabric.event.player.*;
import org.wallentines.midnightcore.fabric.event.server.CustomMessageEvent;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListener {

    @Shadow public ServerPlayer player;

    @Shadow @Final private MinecraftServer server;
    private Component midnight_core_message;
    private PlayerChatEvent midnight_core_currentEvent;
    private Entity midnight_core_currentEntity;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at=@At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {

        PacketSendEvent ev = new PacketSendEvent(player, packet);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at = @At(value = "STORE"), ordinal = 0)
    private Component onChatCreate(Component value) {
        midnight_core_message = value;
        return midnight_core_message;
    }

    @Inject(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), cancellable = true)
    private void onChatSend(TextFilter.FilteredText string, CallbackInfo ci) {
        PlayerChatEvent ev = new PlayerChatEvent(player, string.getFiltered(), midnight_core_message);
        midnight_core_message = null;
        midnight_core_currentEvent = ev;

        Event.invoke(ev);

        if(ev.isCancelled()) {
            midnight_core_currentEvent = null;
            ci.cancel();
            return;
        }

        if(ev.wasMessageChanged()) {

            server.getPlayerList().broadcastMessage(ev.getBroadcast(), pl ->
                player.shouldFilterMessageTo(pl) ? new TranslatableComponent("chat.type.text", this.player.getDisplayName(), string.getFiltered()): ev.getBroadcast(),
                ChatType.CHAT,
                player.getUUID()
            );

            ci.cancel();
        }
    }

    @ModifyArg(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), index = 0)
    private Component sendChat(Component msg) {
        if(midnight_core_currentEvent == null || midnight_core_currentEvent.isCancelled()) return msg;

        Component out = midnight_core_currentEvent.getBroadcast();
        midnight_core_currentEvent = null;

        return out;
    }

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at=@At("HEAD"), cancellable = true)
    private void onTeleport(double d, double e, double f, float g, float h, Set<ClientboundPlayerPositionPacket.RelativeArgument> set, CallbackInfo ci) {
        Location oldLoc = new Location(ConversionUtil.toIdentifier(player.level.dimension().location()), new Vec3d(player.xOld, player.yOld, player.zOld), player.getRotationVector().x, player.getRotationVector().y);
        Location newLoc = new Location(ConversionUtil.toIdentifier(player.level.dimension().location()), new Vec3d(d, e, f), g, h);

        PlayerTeleportEvent ev = new PlayerTeleportEvent(player, oldLoc, newLoc);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "handleInteract(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;)V", at = @At(value = "STORE"), ordinal = 0)
    private Entity onInteract(Entity ent) {
        midnight_core_currentEntity = ent;
        return ent;
    }

    @ModifyArg(method = "handleInteract(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;)V", at=@At(value="INVOKE", target="Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;dispatch(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket$Handler;)V"), index=0)
    private ServerboundInteractPacket.Handler onInteract(ServerboundInteractPacket.Handler other) {
        return new WrappedHandler(player, midnight_core_currentEntity, other);
    }

    @Inject(method = "handleClientInformation(Lnet/minecraft/network/protocol/game/ServerboundClientInformationPacket;)V", at=@At(value = "HEAD"))
    private void onSettings(ServerboundClientInformationPacket packet, CallbackInfo ci) {

        player.getServer().submit(() -> Event.invoke(new PlayerChangeSettingsEvent(player,
                packet.language(),
                packet.viewDistance(),
                packet.chatVisibility(),
                packet.chatColors(),
                packet.modelCustomisation(),
                packet.mainHand(),
                packet.textFilteringEnabled(),
                packet.allowsListing())));
    }

    @Inject(method = "handleCustomPayload", at=@At(value="HEAD"))
    private void onCustomPayload(ServerboundCustomPayloadPacket packet, CallbackInfo ci) {

        player.getServer().submit(() -> Event.invoke(new CustomMessageEvent(packet.getData(), player)));
    }

    @Redirect(method = "onDisconnect", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"))
    private void onMessageSend(PlayerList instance, Component component, ChatType chatType, UUID uuid) {

        PlayerLeaveEvent event = new PlayerLeaveEvent(player, component);
        player.getServer().submit(() -> Event.invoke(event));

        Component comp = event.getLeaveMessage();
        if(comp != null) instance.broadcastMessage(comp, chatType, uuid);
    }

}
