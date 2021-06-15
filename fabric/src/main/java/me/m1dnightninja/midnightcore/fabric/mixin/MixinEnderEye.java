package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.fabric.api.event.PortalCreateEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderEyeItem.class)
public class MixinEnderEye {

    @Inject(method="useOn", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/state/pattern/BlockPattern$BlockPatternMatch;getFrontTopLeft()Lnet/minecraft/core/BlockPos;"), cancellable = true)
    private void injected(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {

        if(!(useOnContext.getPlayer() instanceof ServerPlayer)) return;

        MPlayer player = FabricPlayer.wrap((ServerPlayer) useOnContext.getPlayer());
        MIdentifier id = ConversionUtil.fromResourceLocation(useOnContext.getLevel().dimension().location());

        PortalCreateEvent event = new PortalCreateEvent(player, MIdentifier.create("minecraft", "the_end"), id);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(InteractionResult.CONSUME);
            cir.cancel();
        }
    }

}
