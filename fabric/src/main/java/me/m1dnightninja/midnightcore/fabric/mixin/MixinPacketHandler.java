package me.m1dnightninja.midnightcore.fabric.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.m1dnightninja.midnightcore.fabric.api.Location;
import me.m1dnightninja.midnightcore.fabric.api.event.*;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.event.WrappedHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinPacketHandler {

    @Shadow public ServerPlayer player;

    private Component message;
    private PlayerChatEvent currentEvent;

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at=@At("HEAD"), cancellable = true)
    private void onSend(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> listener, CallbackInfo ci) {

        PacketSendEvent ev = new PacketSendEvent(player, packet);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at = @At(value = "STORE", ordinal = 0))
    private Component onChatCreate(Component t) {
        message = t;
        return message;
    }

    @Inject(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), cancellable = true)
    private void onChatSend(TextFilter.FilteredText string, CallbackInfo ci) {
        PlayerChatEvent ev = new PlayerChatEvent(player, string.getFiltered(), message);
        message = null;
        currentEvent = ev;

        Event.invoke(ev);

        if(ev.isCancelled()) {
            currentEvent = null;
            ci.cancel();
            return;
        }

        if(ev.wasMessageChanged()) {
            ev.setBroadcast(new TranslatableComponent("chat.type.text", this.player.getDisplayName(), message));
        }
    }

    @ModifyArg(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Ljava/util/function/Function;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), index = 0)
    private Component sendChat(Component msg) {
        if(currentEvent == null || currentEvent.isCancelled()) return msg;

        Component out = currentEvent.getBroadcast();
        currentEvent = null;

        return out;
    }

    @Inject(method = "teleport(DDDFFLjava/util/Set;)V", at=@At("HEAD"), cancellable = true)
    private void onTeleport(double d, double e, double f, float g, float h, Set<ClientboundPlayerPositionPacket.RelativeArgument> set, CallbackInfo ci) {
        Location oldLoc = new Location(player.level.dimension().location(), player.xOld, player.yOld, player.zOld, player.getRotationVector().x, player.getRotationVector().y);
        Location newLoc = new Location(player.level.dimension().location(), d, e, f, g, h);

        PlayerTeleportEvent ev = new PlayerTeleportEvent(player, oldLoc, newLoc);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            ci.cancel();
        }
    }

    private Entity currentEntity;
    @ModifyVariable(method="handleInteract", at=@At(value="STORE"), ordinal = 0)
    private Entity onInteract(Entity ent) {
        currentEntity = ent;
        return ent;
    }

    @ModifyArg(method = "handleInteract", at=@At(value="INVOKE", target="Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;dispatch(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket$Handler;)V"), index=0)
    private ServerboundInteractPacket.Handler onInteract(ServerboundInteractPacket.Handler other) {
        return new WrappedHandler(player, currentEntity, other);
    }

    @Inject(method = "handleClientInformation(Lnet/minecraft/network/protocol/game/ServerboundClientInformationPacket;)V", at=@At(value = "HEAD"))
    private void onSettings(ServerboundClientInformationPacket packet, CallbackInfo ci) {

        Event.invoke(new PlayerChangeSettingsEvent(player,
                ((AccessorClientInformationPacket) packet).getLanguage(),
                ((AccessorClientInformationPacket) packet).getViewDistance(),
                packet.getChatVisibility(),
                packet.getChatColors(),
                packet.getModelCustomisation(),
                packet.getMainHand()
                ));
    }
}
