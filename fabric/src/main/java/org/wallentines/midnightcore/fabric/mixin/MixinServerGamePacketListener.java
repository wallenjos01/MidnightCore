package org.wallentines.midnightcore.fabric.mixin;

import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
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
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.fabric.event.player.*;
import org.wallentines.midnightcore.fabric.event.server.CustomMessageEvent;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.math.Vec3d;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListener {

    @Shadow public ServerPlayer player;

    @Shadow @Final private MinecraftServer server;
    private Entity midnight_core_currentEntity;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at=@At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {

        PacketSendEvent ev = new PacketSendEvent(player, packet);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            ci.cancel();
        }
    }
    @Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Lnet/minecraft/server/network/FilteredText;)V", at=@At(value = "HEAD"), cancellable = true)
    private void onChatSend(ServerboundChatPacket serverboundChatPacket, FilteredText<String> filteredText, CallbackInfo ci) {
        PlayerChatEvent ev = new PlayerChatEvent(player, filteredText.raw());
        Event.invoke(ev);

        if(ev.isCancelled()) {
            ci.cancel();
        }
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

        server.submit(() -> Event.invoke(new PlayerChangeSettingsEvent(player,
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

        server.submit(() -> Event.invoke(new CustomMessageEvent(packet.getData(), player)));
    }

    @Inject(method = "handleResourcePackResponse", at=@At(value="HEAD"))
    private void onCustomPayload(ServerboundResourcePackPacket packet, CallbackInfo ci) {

        MPlayer.ResourcePackStatus status = switch (packet.getAction()) {
            case ACCEPTED -> MPlayer.ResourcePackStatus.ACCEPTED;
            case DECLINED -> MPlayer.ResourcePackStatus.DECLINED;
            case FAILED_DOWNLOAD -> MPlayer.ResourcePackStatus.FAILED;
            case SUCCESSFULLY_LOADED -> MPlayer.ResourcePackStatus.LOADED;
        };

        server.submit(() -> Event.invoke(new ResourcePackStatusEvent(player, status)));
    }

    @Redirect(method = "onDisconnect", at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/resources/ResourceKey;)V"))
    private void onMessageSend(PlayerList instance, Component component, ResourceKey<ChatType> chatType) {

        PlayerLeaveEvent event = new PlayerLeaveEvent(player, component);
        server.submit(() -> Event.invoke(event));

        Component comp = event.getLeaveMessage();
        if(comp != null) instance.broadcastSystemMessage(comp, chatType);
    }

}
