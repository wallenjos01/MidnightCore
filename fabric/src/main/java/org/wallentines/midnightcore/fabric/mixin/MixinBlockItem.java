package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.wallentines.midnightcore.fabric.event.world.BlockPlaceEvent;
import org.wallentines.midnightcore.fabric.event.world.BlockPlacedEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem {

    @Shadow @Nullable protected abstract BlockState getPlacementState(BlockPlaceContext blockPlaceContext);

    @Redirect(method="place",at=@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/BlockItem;getPlacementState(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState onPlace(BlockItem instance, BlockPlaceContext blockPlaceContext) {

        BlockState blockState = getPlacementState(blockPlaceContext);
        if(blockPlaceContext.getLevel().isClientSide) return blockState;

        BlockItem it = (BlockItem) (Object) this;

        ServerPlayer sp = ((ServerPlayer) blockPlaceContext.getPlayer());
        BlockPlaceEvent event = new BlockPlaceEvent(sp, blockPlaceContext.getClickedPos(), it, blockState, blockPlaceContext.getHand());
        Event.invoke(event);

        if(event.isCancelled()) {
            return null;
        }

        return event.getPlacedState();
    }

    @Inject(method="place",at=@At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/InteractionResult;sidedSuccess(Z)Lnet/minecraft/world/InteractionResult;"),
            locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void onPlaced(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir, BlockPlaceContext blockPlaceContext2, BlockState blockState, BlockPos blockPos, Level level) {

        if(blockPlaceContext.getLevel().isClientSide) return;

        BlockItem it = (BlockItem) (Object) this;

        ServerPlayer sp = ((ServerPlayer) blockPlaceContext.getPlayer());
        BlockPlacedEvent event = new BlockPlacedEvent(sp, blockPlaceContext.getClickedPos(), it, blockPlaceContext.getItemInHand(), blockState);
        Event.invoke(event);

        if(event.getPlacedState() != blockState) {
            blockPlaceContext.getLevel().setBlock(blockPlaceContext.getClickedPos(), event.getPlacedState(), 11);
        }
    }

}
