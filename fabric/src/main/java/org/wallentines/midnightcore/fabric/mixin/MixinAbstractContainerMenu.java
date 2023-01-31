package org.wallentines.midnightcore.fabric.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wallentines.midnightcore.fabric.event.player.ContainerClickEvent;
import org.wallentines.midnightlib.event.Event;

import java.lang.reflect.Field;

@Mixin(AbstractContainerMenu.class)
public class MixinAbstractContainerMenu {

    @Inject(method = "doClick", at=@At("HEAD"), cancellable = true)
    private void onClick(int i, int j, ClickType slotActionType, Player playerEntity, CallbackInfo ci) {

        if(playerEntity.level.isClientSide() || i == -1) return;
        AbstractContainerMenu handler = (AbstractContainerMenu) (Object) this;

        Container inv = getInventory(playerEntity, handler);
        if(inv == null) return;

        ContainerClickEvent event = new ContainerClickEvent(inv, handler, (ServerPlayer) playerEntity, i, slotActionType, j);
        Event.invoke(event);

        if(event.isCancelled()) {
            ci.cancel();
        }

    }

    private Container getInventory(Player player, AbstractContainerMenu handler) {

        if(handler instanceof ChestMenu) {
            return ((ChestMenu) handler).getContainer();
        }

        if(handler instanceof InventoryMenu) {
            return player.getInventory();
        }

        for (Field f : handler.getClass().getDeclaredFields()) {
            try {
                if (f.getType() == Container.class || f.getType().isAssignableFrom(Container.class)) {
                    f.setAccessible(true);
                    return (Container) f.get(handler);
                }
            } catch(IllegalAccessException ex) {
                // Ignore
            }
        }

        return null;

    }

}
