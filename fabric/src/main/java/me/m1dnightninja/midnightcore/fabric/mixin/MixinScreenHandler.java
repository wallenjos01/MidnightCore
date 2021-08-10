package me.m1dnightninja.midnightcore.fabric.mixin;

import me.m1dnightninja.midnightcore.fabric.event.ContainerClickEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(AbstractContainerMenu.class)
public class MixinScreenHandler {

    //private static final List<ClickType> REMOVE = Arrays.asList( ClickType.QUICK_MOVE, ClickType.THROW );

    @Inject(method = "doClick", at=@At("HEAD"), cancellable = true)
    private void onClick(int i, int j, ClickType slotActionType, Player playerEntity, CallbackInfo ci) {

        if(playerEntity.level.isClientSide()) return;
        AbstractContainerMenu handler = (AbstractContainerMenu) (Object) this;

        Container inv = getInventory(handler);
        if(inv == null) return;

        ContainerClickEvent event = new ContainerClickEvent(inv, handler, (ServerPlayer) playerEntity, i, slotActionType, j);
        Event.invoke(event);

        if(event.isCancelled()) {
            /*if(REMOVE.contains(slotActionType)) {
                cir.setReturnValue(inv.getItem(i));
            } else {
                cir.setReturnValue(ItemStack.EMPTY);
            }*/
            ci.cancel();
        }

    }

    private Container getInventory(AbstractContainerMenu handler) {

        if(handler instanceof ChestMenu) {
            return ((ChestMenu) handler).getContainer();
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
