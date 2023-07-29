package org.wallentines.mcore.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.mcore.event.LoginQueryEvent;
import org.wallentines.mcore.messaging.FabricServerLoginNegotiator;
import org.wallentines.midnightlib.event.Event;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class MixinServerLoginPacketListener {


    @Shadow
    @Final
    Connection connection;
    @Shadow @Final
    MinecraftServer server;
    @Shadow @Nullable
    GameProfile gameProfile;
    @Shadow ServerLoginPacketListenerImpl.State state;

    @Shadow public abstract String getUserName();

    @Unique
    private FabricServerLoginNegotiator mcore$negotiator;

    @Inject(method="tick", at=@At("TAIL"))
    private void onTick(CallbackInfo ci) {

        if(state == ServerLoginPacketListenerImpl.State.NEGOTIATING && mcore$negotiator.itemsInQueue() == 0) {
            state = ServerLoginPacketListenerImpl.State.READY_TO_ACCEPT;
        }
    }

    @Inject(method="handleAcceptedLogin", at=@At(value="INVOKE", target="Lnet/minecraft/network/protocol/login/ClientboundGameProfilePacket;<init>(Lcom/mojang/authlib/GameProfile;)V"), cancellable = true)
    private void onAccepted(CallbackInfo ci) {

        // Check to see if negotiating has begun already
        if(mcore$negotiator == null) {

            mcore$negotiator = new FabricServerLoginNegotiator(gameProfile == null ? null : gameProfile.getId(), getUserName(), connection);
            state = ServerLoginPacketListenerImpl.State.NEGOTIATING;

            // Send "Negotiating" packets
            Event.invoke(new LoginQueryEvent(server, gameProfile, mcore$negotiator));
            ci.cancel();

        }
    }

    @Redirect(method="handleCustomQueryPacket", at=@At(value = "INVOKE", target="Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private void nopDisconnect(ServerLoginPacketListenerImpl instance, Component component) {
        // Do nothing
    }

    @Inject(method="handleCustomQueryPacket", at=@At("HEAD"))
    private void onCustomQuery(ServerboundCustomQueryPacket packet, CallbackInfo ci) {

        // Handle Negotiation responses
        if(mcore$negotiator != null) {
            mcore$negotiator.handlePacket(packet.getTransactionId(), packet.getData());
        }
    }

}
