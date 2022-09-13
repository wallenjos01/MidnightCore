package org.wallentines.midnightcore.spigot.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.midnightcore.common.item.ItemConverter;
import org.wallentines.midnightlib.Version;

public final class ItemConverters {

    public static ItemConverter getItemConverter(Version version) {

        if(version.getMinorVersion() < 13) {
            return LegacyItem::new;
        }

        return SpigotItem::new;
    }

    public static AbstractItem convertItem(ItemStack is) {

        if(MidnightCoreAPI.getInstance().getGameVersion().getMinorVersion() < 13) {
            return new LegacyItem(is);
        }

        return new SpigotItem(is);
    }


    public static ItemStack getInternal(MItemStack is) {

        if(MidnightCoreAPI.getInstance().getGameVersion().getMinorVersion() < 13) {
            return ((LegacyItem) is).getInternal();
        }

        return ((SpigotItem) is).getInternal();
    }

    public static void giveItem(Player pl, MItemStack it) {
        if(it instanceof LegacyItem) {
            pl.getInventory().addItem(((LegacyItem) it).getInternal());
            return;
        }
        pl.getInventory().addItem(((SpigotItem) it).getInternal());
    }

    public static void giveItem(Player pl, MItemStack it, int slot) {
        if(it instanceof LegacyItem) {
            pl.getInventory().setItem(slot, ((LegacyItem) it).getInternal());
            return;
        }
        pl.getInventory().setItem(slot, ((SpigotItem) it).getInternal());
    }

}
