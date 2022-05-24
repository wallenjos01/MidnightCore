package org.bukkit.craftbukkit.v1_12_R1.inventory;

import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static net.minecraft.server.v1_12_R1.ItemStack getHandle(ItemStack is) {

        return ((CraftItemStack) is).handle;
    }


}
