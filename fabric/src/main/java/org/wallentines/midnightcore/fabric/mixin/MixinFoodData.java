package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.player.PlayerFoodLevelChangeEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(FoodData.class)
public abstract class MixinFoodData {

    @Shadow private int foodLevel;
    @Shadow private int lastFoodLevel;
    @Shadow public abstract void eat(int food, float f);
    @Shadow private float saturationLevel;

    private ServerPlayer mcore$player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onUpdate(Player ent, CallbackInfo info) {
        if(mcore$player == null && ent instanceof ServerPlayer) {
            mcore$player = (ServerPlayer) ent;
        }
    }

    @Redirect(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"))
    private void onEat(FoodData hungerManager, int food, float f) {

        if(mcore$player == null) return;

        int oldFood = foodLevel;
        int newFood = Math.min(food + this.foodLevel, 20);

        PlayerFoodLevelChangeEvent event = new PlayerFoodLevelChangeEvent(mcore$player, oldFood, newFood);
        Event.invoke(event);

        if(!event.isCancelled()) {
            eat(event.getNewFoodLevel() - oldFood, f);
        }

        mcore$player.connection.send(new ClientboundSetHealthPacket(mcore$player.getHealth(), foodLevel, saturationLevel));
    }

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;foodLevel:I", opcode = Opcodes.PUTFIELD))
    private void onChanged(FoodData hungerManager, int value) {

        if (mcore$player == null) {
            hungerManager.setFoodLevel(value);
            return;
        }

        PlayerFoodLevelChangeEvent event = new PlayerFoodLevelChangeEvent(mcore$player, lastFoodLevel, value);
        Event.invoke(event);

        if (!event.isCancelled()) {

            hungerManager.setFoodLevel(event.getNewFoodLevel());
        }
    }

}
