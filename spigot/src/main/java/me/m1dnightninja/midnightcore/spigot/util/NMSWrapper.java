package me.m1dnightninja.midnightcore.spigot.util;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.version.v1_16.NMSUtil_16;
import me.m1dnightninja.midnightcore.spigot.version.v1_17.NMSUtil_17;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class NMSWrapper {

    private static NMSUtil CACHED_UTIL;

    private static NMSUtil getUtil() {

        if(CACHED_UTIL == null) {

            if(ReflectionUtil.MAJOR_VERISON <= 16) {
                CACHED_UTIL = new NMSUtil_16();
            } else {
                CACHED_UTIL = new NMSUtil_17();
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

    public interface NMSUtil {

        GameProfile getGameProfile(Player pl);
        void sendMessage(Player pl, MComponent comp);
    }


}
