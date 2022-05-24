package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.event.world.PortalCreateEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(EnderEyeItem.class)
public class MixinEnderEyeItem {

    @Inject(method="useOn", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch;getFrontTopLeft()Lnet/minecraft/core/BlockPos;"), cancellable = true)
    private void injected(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {

        if(!(useOnContext.getPlayer() instanceof ServerPlayer player)) return;

        MinecraftServer server = player.getServer();
        if(server == null) return;

        ServerLevel target = player.getServer().getLevel(Level.END);

        PortalCreateEvent event = new PortalCreateEvent(player, target, player.getLevel());
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(InteractionResult.CONSUME);
            cir.cancel();
        }
    }

}
