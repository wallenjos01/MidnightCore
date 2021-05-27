package me.m1dnightninja.midnightcore.fabric.mixin;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public interface AccessorAbstractContainerMenu {
    @Accessor
    List<ContainerListener> getContainerListeners();
}
