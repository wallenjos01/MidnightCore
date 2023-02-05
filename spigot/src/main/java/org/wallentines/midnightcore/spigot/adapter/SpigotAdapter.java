package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.mdcfg.ConfigSection;

import java.util.UUID;

public interface SpigotAdapter {

    boolean init();

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

    ItemStack setupInternal(ItemStack item);

    void addTickable(Runnable runnable);

    @Deprecated
    default String toJsonString(MComponent component) {

        return component.toJSONString();
    }

    @Deprecated
    default ConfigProvider getJsonSerializer() {

        return org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider.INSTANCE;
    }

    default ItemStack getItemInMainHand(Player pl) {

        return pl.getInventory().getItemInMainHand();
    }

    default ItemStack getItemInOffHand(Player pl) {

        return pl.getInventory().getItemInOffHand();
    }

    UUID NIL_UUID = new UUID(0L, 0L);
}
