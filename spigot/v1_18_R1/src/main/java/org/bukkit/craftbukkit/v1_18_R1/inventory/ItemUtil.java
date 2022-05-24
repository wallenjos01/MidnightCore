package org.bukkit.craftbukkit.v1_18_R1.inventory;

import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static net.minecraft.world.item.ItemStack getHandle(ItemStack is) {

        return ((CraftItemStack) is).handle;
    }

}
