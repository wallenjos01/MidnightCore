package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.api.event.PlayerInteractEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerGameMode {

    @Inject(method = "useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at=@At("HEAD"), cancellable = true)
    private void onRightClick(ServerPlayer player, Level level, ItemStack item, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {

        PlayerInteractEvent event = new PlayerInteractEvent(player, item, hand, null);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.setReturnValue(event.shouldSwingArm() ? InteractionResult.SUCCESS : InteractionResult.PASS);
            ci.cancel();
        }

    }

    @Inject(method = "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at=@At("HEAD"), cancellable = true)
    private void onRightClickBlock(ServerPlayer player, Level level, ItemStack item, InteractionHand hand, BlockHitResult res, CallbackInfoReturnable<InteractionResult> ci) {

        PlayerInteractEvent event = new PlayerInteractEvent(player, item, hand, res);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.setReturnValue(event.shouldSwingArm() ? InteractionResult.SUCCESS : InteractionResult.PASS);
            ci.cancel();
        }

    }



}
