package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.api.event.BlockPlaceEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class MixinBlockItem {

    @Inject(method="place",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/item/BlockItem;placeBlock(Lnet/minecraft/world/item/context/BlockPlaceContext;Lnet/minecraft/world/level/block/state/BlockState;)Z"),cancellable = true)
    private void onPlace(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir) {

        if(!(blockPlaceContext.getPlayer() instanceof ServerPlayer)) return;

        BlockItem it = (BlockItem) (Object) this;

        BlockPlaceEvent event = new BlockPlaceEvent((FabricPlayer) FabricPlayer.wrap((ServerPlayer) blockPlaceContext.getPlayer()), blockPlaceContext.getClickedPos(), it);
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

}
