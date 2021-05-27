package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.api.event.BlockBreakEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerInteractEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class MixinServerGameMode {


    @Shadow public ServerPlayer player;
    @Shadow public ServerLevel level;

    @Inject(method = "destroyBlock", at=@At(value="INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)V"), cancellable = true)
    private void onDestroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {

        BlockBreakEvent event = new BlockBreakEvent((FabricPlayer) FabricPlayer.wrap(player), blockPos, level.getBlockState(blockPos));
        Event.invoke(event);

        if(event.isCancelled()) {
            cir.setReturnValue(false);
            cir.cancel();
        }

    }

    @Inject(method = "useItem(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at=@At("HEAD"), cancellable = true)
    private void onRightClick(ServerPlayer player, Level level, ItemStack item, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {

        PlayerInteractEvent event = new PlayerInteractEvent(player, item, hand, ServerboundInteractPacket.Action.INTERACT, null);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.setReturnValue(event.shouldSwingArm() ? InteractionResult.SUCCESS : InteractionResult.PASS);
            ci.cancel();
        }

    }

    @Inject(method = "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at=@At("HEAD"), cancellable = true)
    private void onRightClickBlock(ServerPlayer player, Level level, ItemStack item, InteractionHand hand, BlockHitResult res, CallbackInfoReturnable<InteractionResult> ci) {

        PlayerInteractEvent event = new PlayerInteractEvent(player, item, hand, ServerboundInteractPacket.Action.INTERACT, res);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.setReturnValue(event.shouldSwingArm() ? InteractionResult.SUCCESS : InteractionResult.PASS);
            ci.cancel();
        }

    }



}
