package org.wallentines.midnightcore.spigot.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.common.item.AbstractItem;
import org.wallentines.midnightlib.Version;

public final class ItemHelper {

    public static MItemStack.Factory getItemConverter(Version version) {

        if(version.getMinorVersion() < 13) {
            return LegacyItem::new;
        }

        return SpigotItem::new;
    }

    private static boolean isLegacy() {

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        int minorVersion = api == null ? 13 : api.getGameVersion().getMinorVersion();
        return minorVersion < 13;
    }

    public static AbstractItem convertItem(ItemStack is) {

        if(isLegacy()) {
            return new LegacyItem(is);
        }

        return new SpigotItem(is);
    }


    public static ItemStack getInternal(MItemStack is) {

        if(isLegacy()) {
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
