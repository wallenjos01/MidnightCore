package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.event.player.PlayerInteractEvent;
import org.wallentines.midnightcore.fabric.event.world.BlockBreakEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerPlayerGameMode {

    @Final @Shadow protected ServerPlayer player;
    @Shadow protected ServerLevel level;

    @Inject(method = "destroyBlock", at=@At(value="INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V"), cancellable = true)
    private void onDestroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {

        BlockBreakEvent event = new BlockBreakEvent(player, blockPos, level.getBlockState(blockPos));
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }

    }

    @Inject(method = "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at=@At("HEAD"), cancellable = true)
    private void onRightClickBlock(ServerPlayer player, Level level, ItemStack item, InteractionHand hand, BlockHitResult res, CallbackInfoReturnable<InteractionResult> ci) {

        if(!item.isEmpty() && item.getItem() instanceof BucketItem) return;

        PlayerInteractEvent event = new PlayerInteractEvent(player, item, hand, PlayerInteractEvent.InteractionType.INTERACT_BLOCK, res);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.setReturnValue(event.shouldSwingArm() ? InteractionResult.SUCCESS : InteractionResult.PASS);
            ci.cancel();
        }

    }

}
