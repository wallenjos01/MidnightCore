package me.m1dnightninja.midnightcore.spigot.util;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.spigot.version.NMSUtil_Other;
import me.m1dnightninja.midnightcore.spigot.version.v1_16.NMSUtil_16;
import me.m1dnightninja.midnightcore.spigot.version.v1_17.NMSUtil_17;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class NMSUtil {

    private static NMSHandler CACHED_UTIL;

    private static NMSHandler getUtil() {

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

    public static void sendTitle(Player pl, MTitle t) {
        getUtil().sendTitle(pl, t);
    }

    public static void sendActionBar(Player pl, MActionBar ab) {
        getUtil().sendActionBar(pl, ab);
    }

    public static ConfigSection getItemTag(ItemStack im) { return getUtil().getItemTag(im); }
    public static ItemStack setItemTag(ItemStack im, ConfigSection tag) { return getUtil().setItemTag(im, tag); }

    public interface NMSHandler {

        GameProfile getGameProfile(Player pl);
        void sendMessage(Player pl, MComponent comp);
        void sendActionBar(Player pl, MActionBar ab);
        void sendTitle(Player pl, MTitle title);

        ConfigSection getItemTag(ItemStack is);
        ItemStack setItemTag(ItemStack is, ConfigSection tag);
    }


}
