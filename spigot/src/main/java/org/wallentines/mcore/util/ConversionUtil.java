package org.wallentines.mcore.util;

import org.wallentines.mcore.Player;
import org.wallentines.mcore.SpigotPlayer;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.item.SpigotItem;

public class ConversionUtil {

    public static SpigotPlayer validate(Player player) {
        if(!(player instanceof SpigotPlayer)) {
            throw new IllegalArgumentException("Attempt to access non-Spigot Player!");
        }
        return (SpigotPlayer) player;
    }

    public static SpigotItem validate(ItemStack itemStack) {
        if(!(itemStack instanceof SpigotItem)) {
            throw new IllegalArgumentException("Attempt to access non-Spigot Player!");
        }
        return (SpigotItem) itemStack;

    }

}
