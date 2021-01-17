package me.m1dnightninja.midnightcore.fabric.mixin;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.api.event.PacketSendEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerChatEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @ModifyVariable(method = "handleChat(Ljava/lang/String;)V", at = @At(value = "STORE", ordinal = 0))
    private Component onChatCreate(Component t) {
        message = t;
        return message;
    }

    @Inject(method = "handleChat(Ljava/lang/String;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), cancellable = true)
    private void onChatSend(String string, CallbackInfo ci) {
        PlayerChatEvent ev = new PlayerChatEvent(player, string, message);
        message = null;
        currentEvent = ev;

        Event.invoke(ev);

        if(ev.isCancelled()) {
            currentEvent = null;
            ci.cancel();
            return;
        }

        if(ev.wasMessageChanged() && !ev.getMessage().equals(string)) {
            ev.setBroadcast(new TranslatableComponent("chat.type.text", this.player.getDisplayName(), message));
        }
    }

    @ModifyArg(method = "handleChat(Ljava/lang/String;)V", at=@At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;broadcastMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/ChatType;Ljava/util/UUID;)V"), index = 0)
    private Component sendChat(Component msg) {
        if(currentEvent == null || currentEvent.isCancelled()) return msg;

        Component out = currentEvent.getBroadcast();
        currentEvent = null;

        return out;
    }
}
