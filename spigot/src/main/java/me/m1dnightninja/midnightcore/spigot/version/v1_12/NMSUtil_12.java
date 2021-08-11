package me.m1dnightninja.midnightcore.spigot.version.v1_12;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.spigot.util.NMSUtil;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class NMSUtil_12 implements NMSUtil.NMSHandler {

    private static final Class<?> craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
    private static final Class<?> craftItemStack = ReflectionUtil.getCraftBukkitClass("inventory.CraftItemStack");

    private static final Class<?> mojangsonParser = ReflectionUtil.getNMSClass("MojangsonParser");
    private static final Class<?> nbtTagCompound = ReflectionUtil.getNMSClass("NBTTagCompound");
    private static final Class<?> itemStack = ReflectionUtil.getNMSClass("ItemStack");

    private static final Class<?> entityPlayer = ReflectionUtil.getNMSClass("EntityPlayer");
    private static final Class<?> packet = ReflectionUtil.getNMSClass("Packet");
    private static final Class<?> packetPlayOutChat = ReflectionUtil.getNMSClass("PacketPlayOutChat");
    private static final Class<?> playerConnection = ReflectionUtil.getNMSClass("PlayerConnection");
    private static final Class<?> iChatBaseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");
    private static final Class<?> chatSerializer = ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer");
    private static final Class<?> chatMessageType = ReflectionUtil.getNMSClass("ChatMessageType");

    private static final Method getProfile = ReflectionUtil.getMethod(craftPlayer, "getProfile");
    private static final Method getHandle = ReflectionUtil.getMethod(craftPlayer, "getHandle");
    private static final Method sendPacket = ReflectionUtil.getMethod(playerConnection, "sendPacket", packet);
    private static final Method fromJson = ReflectionUtil.getMethod(chatSerializer, "a", String.class);
    private static final Method setTag = ReflectionUtil.getMethod(itemStack, "setTag", nbtTagCompound);
    private static final Method getTag = ReflectionUtil.getMethod(itemStack, "getTag");
    private static final Method parseTag = ReflectionUtil.getMethod(mojangsonParser, "parse", String.class);

    private static final Object ChatType_SYSTEM = ReflectionUtil.getEnumValue(chatMessageType, "SYSTEM");
    private static final Object ChatType_ACTION_BAR = ReflectionUtil.getEnumValue(chatMessageType, "GAME_INFO");

    private static final Method asNMSCopy = ReflectionUtil.getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
    private static final Method asBukkitCopy = ReflectionUtil.getMethod(craftItemStack, "asBukkitCopy", itemStack);

    private static final Field playerConnectionField = ReflectionUtil.getField(entityPlayer, "playerConnection");

    private static final Constructor<?> packetPlayOutChatConstructor = ReflectionUtil.getConstructor(packetPlayOutChat, iChatBaseComponent, chatMessageType);

    public GameProfile getGameProfile(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (GameProfile) ReflectionUtil.callMethod(craftp, getProfile, false);

    }

    @Override
    public void sendMessage(Player player, MComponent comp) {

        sendMessage(player, comp, ChatType_SYSTEM);
    }

    @Override
    public void sendActionBar(Player pl, MActionBar ab) {
        sendMessage(pl, ab.getText(), ChatType_ACTION_BAR);
    }

    private void sendMessage(Player pl, MComponent cmp, Object data) {

        Object craftp = ReflectionUtil.castTo(pl, craftPlayer);
        Object ep = ReflectionUtil.callMethod(craftp, getHandle, false);
        Object pc = ReflectionUtil.getFieldValue(ep, playerConnectionField, false);

        Object message = ReflectionUtil.callMethod(chatSerializer, fromJson, false, MComponent.Serializer.toJsonString(cmp));
        Object pck = ReflectionUtil.construct(packetPlayOutChatConstructor, message, data);

        ReflectionUtil.callMethod(pc, sendPacket, false, pck);
    }

    @Override
    public void sendTitle(Player pl, MTitle title) {

        String text = title.getText().toLegacyText(false);

        if(title.getOptions().clear) {
            pl.resetTitle();
        }

        if(title.getOptions().subtitle) {
            pl.sendTitle(text, null, title.getOptions().fadeIn, title.getOptions().stay, title.getOptions().fadeOut);
        } else {
            pl.sendTitle(null, text, title.getOptions().fadeIn, title.getOptions().stay, title.getOptions().fadeOut);
        }

    }

    @Override
    public ConfigSection getItemTag(ItemStack is) {

        Object mis = ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, is);
        Object compound = ReflectionUtil.callMethod(mis, getTag, false);

        return JsonConfigProvider.INSTANCE.loadFromString(compound.toString());
    }

    @Override
    public ItemStack setItemTag(ItemStack is, ConfigSection tag) {

        Object mis = ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, is);
        String json = tag.toNBT();

        Object cmp = ReflectionUtil.callMethod(mojangsonParser, parseTag, false, json);
        ReflectionUtil.callMethod(mis, setTag, false, cmp);

        return (ItemStack) ReflectionUtil.callMethod(craftItemStack, asBukkitCopy, false, mis);
    }

}
