package me.m1dnightninja.midnightcore.spigot.util;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public class NMSUtil {

    public static final UUID nullUid = new UUID(0L, 0L);

    private static final Class<?> craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");

    private static final Class<?> serverPlayer = ReflectionUtil.getNMSClass("EntityPlayer");
    private static final Class<?> chatMessageType = ReflectionUtil.getNMSClass("ChatMessageType");
    private static final Class<?> baseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer");
    private static final Class<?> chatSerializer = ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer");

    private static final Method getHandle = ReflectionUtil.getMethod(craftPlayer, "getHandle");
    private static final Method getProfile = ReflectionUtil.getMethod(craftPlayer, "getProfile");
    private static final Method sendMessage = ReflectionUtil.getMethod(serverPlayer, "a", baseComponent, chatMessageType, UUID.class);
    private static final Method fromJson = ReflectionUtil.getMethod(chatSerializer, "a", String.class);

    private static final Object ChatType_SYSTEM = ReflectionUtil.getEnumValue(chatMessageType, "SYSTEM");

    public static GameProfile getGameProfile(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (GameProfile) ReflectionUtil.callMethod(craftp, getProfile, false);

    }

    public static void sendMessage(Player player, MComponent comp) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        Object nmsPl = ReflectionUtil.callMethod(craftp, getHandle, false);

        Object message = ReflectionUtil.callMethod(chatSerializer, fromJson, false, MComponent.Serializer.toJson(comp));

        ReflectionUtil.callMethod(nmsPl, sendMessage, false, message, ChatType_SYSTEM, nullUid);

    }

}
