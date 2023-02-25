package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightlib.event.Event;


@Mixin(BucketItem.class)
public class MixinBucketItem {


    @SuppressWarnings("ConstantConditions")
    @Inject(method="use", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getBlockPos()Lnet/minecraft/core/BlockPos;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void onUse(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir, ItemStack itemStack, BlockHitResult blockHitResult) {

        if(blockHitResult == null || blockHitResult.getType() == HitResult.Type.MISS || level.isClientSide) return;
        ItemStack is = player.getItemInHand(interactionHand);

        PlayerInteractEvent event = new PlayerInteractEvent((ServerPlayer) player, is, interactionHand, PlayerInteractEvent.InteractionType.INTERACT, blockHitResult);
        level.getServer().submit(() -> Event.invoke(event));

        if(event.isCancelled()) {
            cir.setReturnValue(InteractionResultHolder.pass(is));
            cir.cancel();
        }

    }

}
