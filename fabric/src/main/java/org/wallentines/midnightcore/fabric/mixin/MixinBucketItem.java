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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightlib.event.Event;


@Mixin(BucketItem.class)
public class MixinBucketItem {

    private BlockHitResult midnight_core_result;

    @ModifyVariable(method = "use", at=@At(value="STORE"), ordinal = 0)
    private BlockHitResult onUseCalculate(BlockHitResult res) {

        if(res.getType() != HitResult.Type.MISS) {
            midnight_core_result = res;
        }

        return res;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method="use", at=@At(value = "INVOKE", target = "Lnet/minecraft/world/phys/BlockHitResult;getBlockPos()Lnet/minecraft/core/BlockPos;"), cancellable = true)
    private void onUse(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

        if(midnight_core_result == null || level.isClientSide) return;
        ItemStack is = player.getItemInHand(interactionHand);

        BlockHitResult res = midnight_core_result;

        PlayerInteractEvent event = new PlayerInteractEvent((ServerPlayer) player, is, interactionHand, PlayerInteractEvent.InteractionType.INTERACT, res);
        level.getServer().submit(() -> Event.invoke(event));

        if(event.isCancelled()) {
            cir.setReturnValue(InteractionResultHolder.pass(is));
            cir.cancel();
        }

        midnight_core_result = null;
    }

}
