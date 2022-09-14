package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.event.entity.EntitySpawnEvent;
import org.wallentines.midnightcore.fabric.module.dynamiclevel.DynamicLevelContext;
import org.wallentines.midnightlib.event.Event;

import java.util.Iterator;
import java.util.List;

@Mixin(ServerLevel.class)
public class MixinServerLevel {

    @Shadow @Final List<ServerPlayer> players;

    @ModifyVariable(method="<init>", at=@At("STORE"), ordinal=1)
    private long injectSeed(long m) {

        ServerLevel lvl = (ServerLevel) (Object) this;
        if(lvl instanceof DynamicLevelContext.DynamicLevel dl) {
            return dl.getSeed();
        }

        return m;
    }

    @Redirect(method="advanceWeatherCycle()V", at=@At(value = "INVOKE", target="Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"))
    private void injectTick(PlayerList list, Packet<?> packet) {
        list.broadcastAll(packet, ((ServerLevel) (Object) this).dimension());
    }

    @Redirect(method="destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V", at=@At(value = "INVOKE", target="Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<ServerPlayer> injectDestroyBlock(List<ServerPlayer> pls) {
        return players.iterator();
    }

    @Inject(method="addFreshEntity", at=@At("HEAD"), cancellable = true)
    private void onSpawn(Entity entity, CallbackInfoReturnable<Boolean> cir) {

        EntitySpawnEvent ev = new EntitySpawnEvent(entity);
        Event.invoke(ev);

        if(ev.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

}