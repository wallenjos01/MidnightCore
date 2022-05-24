package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;

import java.util.UUID;

public interface SpigotAdapter {

    GameProfile getGameProfile(Player pl);


    void sendMessage(Player pl, MComponent comp);

    void sendActionBar(Player pl, MComponent comp);


    void sendTitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut);

    void sendSubtitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut);

    void clearTitles(Player pl);


    void setTag(ItemStack is, ConfigSection sec);

    ConfigSection getTag(ItemStack is);

    boolean hasOpLevel(Player pl, int lvl);

    ConfigSection getTag(Player pl);

    void loadTag(Player pl, ConfigSection tag);

    SkinUpdater getSkinUpdater();

    boolean isVersionSupported(String str);

    default ItemStack getItemInMainHand(Player pl) {

        return pl.getInventory().getItemInMainHand();
    }

    default ItemStack getItemInOffHand(Player pl) {

        return pl.getInventory().getItemInOffHand();
    }

    UUID NIL_UUID = new UUID(0L, 0L);
}
