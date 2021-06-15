package me.m1dnightninja.midnightcore.spigot.version.v1_17;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class NMSUtil_17 implements NMSWrapper.NMSUtil {

    public static final UUID nullUid = new UUID(0L, 0L);

    private static final Class<?> craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
    private static final Method getHandle = ReflectionUtil.getMethod(craftPlayer, "getHandle");
    private static final Method getProfile = ReflectionUtil.getMethod(craftPlayer, "getProfile");

    public GameProfile getGameProfile(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (GameProfile) ReflectionUtil.callMethod(craftp, getProfile, false);

    }

    public void sendMessage(Player player, MComponent comp) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        EntityPlayer nmsPl = (EntityPlayer) ReflectionUtil.callMethod(craftp, getHandle, false);

        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(MComponent.Serializer.toJson(comp));
        nmsPl.sendMessage(message, nullUid);

    }

}
