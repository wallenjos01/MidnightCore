package me.m1dnightninja.midnightcore.spigot.util;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.spigot.version.NMSUtil_Other;
import me.m1dnightninja.midnightcore.spigot.version.v1_16.NMSUtil_16;
import me.m1dnightninja.midnightcore.spigot.version.v1_17.NMSUtil_17;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NMSWrapper {

    private static NMSUtil CACHED_UTIL;

    private static NMSUtil getUtil() {

        if(CACHED_UTIL == null) {

            try {
                if (ReflectionUtil.MAJOR_VERISON <= 16) {
                    CACHED_UTIL = new NMSUtil_16();
                } else {
                    CACHED_UTIL = new NMSUtil_17();
                }
            } catch (IllegalStateException ex) {

                ex.printStackTrace();

                MidnightCoreAPI.getLogger().warn("Warning: Unable to find supported NMS Util! Functions involving Skins and RGB text may not work properly!");
                CACHED_UTIL = new NMSUtil_Other();
            }
        }

        return CACHED_UTIL;
    }

    public static GameProfile getGameProfile(Player pl) {

        return getUtil().getGameProfile(pl);

    }

    public static void sendMessage(Player pl, MComponent comp) {

        getUtil().sendMessage(pl, comp);
    }

    public static void sendTitle(Player pl, Title t) {
        getUtil().sendTitle(pl, t);
    }

    public static void sendActionBar(Player pl, ActionBar ab) {
        getUtil().sendActionBar(pl, ab);
    }

    public static ConfigSection getItemTag(ItemStack im) { return getUtil().getItemTag(im); }
    public static ItemStack setItemTag(ItemStack im, ConfigSection tag) { return getUtil().setItemTag(im, tag); }

    public interface NMSUtil {

        GameProfile getGameProfile(Player pl);
        void sendMessage(Player pl, MComponent comp);
        void sendActionBar(Player pl, ActionBar ab);
        void sendTitle(Player pl, Title title);

        ConfigSection getItemTag(ItemStack is);
        ItemStack setItemTag(ItemStack is, ConfigSection tag);
    }


}
