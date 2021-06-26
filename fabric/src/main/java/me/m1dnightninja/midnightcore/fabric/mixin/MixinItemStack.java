package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.api.event.PlayerInteractEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Inject(method="use",at=@At("HEAD"),cancellable = true)
    private void onUse(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if(player.level.isClientSide()) return;

        ItemStack item = player.getItemInHand(interactionHand);
        if(!item.isEmpty() && item.getItem() instanceof BucketItem) return;

        PlayerInteractEvent event = new PlayerInteractEvent((ServerPlayer) player, item, interactionHand, PlayerInteractEvent.InteractionType.INTERACT, null);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(event.shouldSwingArm() ? InteractionResultHolder.success(item) : InteractionResultHolder.pass(item));
            cir.cancel();
        }

    }

}
