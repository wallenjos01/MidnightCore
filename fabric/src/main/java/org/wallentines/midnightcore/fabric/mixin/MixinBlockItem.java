package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wallentines.midnightcore.fabric.event.world.BlockPlaceEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(BlockItem.class)
public class MixinBlockItem {

    @Inject(method="place",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"),cancellable = true)
    private void onPlace(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir) {

        if(blockPlaceContext.getLevel().isClientSide) return;

        BlockItem it = (BlockItem) (Object) this;

        ServerPlayer sp = ((ServerPlayer) blockPlaceContext.getPlayer());
        BlockPlaceEvent event = new BlockPlaceEvent(sp, blockPlaceContext.getClickedPos(), it);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

}
