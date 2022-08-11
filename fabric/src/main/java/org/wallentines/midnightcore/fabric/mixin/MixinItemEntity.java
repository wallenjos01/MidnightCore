package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.player.PlayerPickupItemEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity {

    @Shadow public abstract ItemStack getItem();

    @Inject(method="playerTouch", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"), cancellable = true)
    private void onTouch(Player player, CallbackInfo ci) {

        ItemEntity ent = (ItemEntity) (Object) this;

        PlayerPickupItemEvent event = new PlayerPickupItemEvent((ServerPlayer) player, ent);
        Event.invoke(event);

        if(event.isCancelled()) {
            ItemStack is = getItem();
            is.setCount(0);
            ci.cancel();
        }
    }
}
