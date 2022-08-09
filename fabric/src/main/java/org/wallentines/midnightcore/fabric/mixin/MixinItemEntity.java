package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.player.PlayerPickupItemEvent;
import org.wallentines.midnightlib.event.Event;

@Mixin(ItemEntity.class)
public class MixinItemEntity {

    @Inject(method="playerTouch", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/item/ItemEntity;getItem()Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void onTouch(Player player, CallbackInfo ci) {

        ItemEntity ent = (ItemEntity) (Object) this;

        PlayerPickupItemEvent event = new PlayerPickupItemEvent((ServerPlayer) player, ent);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.cancel();
        }
    }
}
