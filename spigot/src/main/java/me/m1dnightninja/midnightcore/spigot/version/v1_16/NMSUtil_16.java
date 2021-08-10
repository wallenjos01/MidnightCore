package me.m1dnightninja.midnightcore.spigot.version.v1_16;

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

import java.lang.reflect.Method;
import java.util.UUID;

public class NMSUtil_16 implements NMSUtil.NMSHandler {

    public static final UUID nullUid = new UUID(0L, 0L);

    private static final Class<?> craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
    private static final Class<?> craftItemStack = ReflectionUtil.getCraftBukkitClass("inventory.CraftItemStack");


    private static final Class<?> serverPlayer = ReflectionUtil.getNMSClass("EntityPlayer");
    private static final Class<?> chatMessageType = ReflectionUtil.getNMSClass("ChatMessageType");
    private static final Class<?> baseComponent = ReflectionUtil.getNMSClass("IChatBaseComponent");
    private static final Class<?> chatSerializer = ReflectionUtil.getNMSClass("IChatBaseComponent$ChatSerializer");
    private static final Class<?> mojangsonParser = ReflectionUtil.getNMSClass("MojangsonParser");
    private static final Class<?> nbtTagCompound = ReflectionUtil.getNMSClass("NBTTagCompound");
    private static final Class<?> itemStack = ReflectionUtil.getNMSClass("ItemStack");

    private static final Method getHandle = ReflectionUtil.getMethod(craftPlayer, "getHandle");
    private static final Method getProfile = ReflectionUtil.getMethod(craftPlayer, "getProfile");
    private static final Method sendMessage = ReflectionUtil.getMethod(serverPlayer, "a", baseComponent, chatMessageType, UUID.class);
    private static final Method fromJson = ReflectionUtil.getMethod(chatSerializer, "a", String.class);
    private static final Method setTag = ReflectionUtil.getMethod(itemStack, "setTag", nbtTagCompound);
    private static final Method getTag = ReflectionUtil.getMethod(itemStack, "getTag");
    private static final Method asString = ReflectionUtil.getMethod(nbtTagCompound, "asString");
    private static final Method parseTag = ReflectionUtil.getMethod(mojangsonParser, "parse", String.class);

    private static final Method asNMSCopy = ReflectionUtil.getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
    private static final Method asBukkitCopy = ReflectionUtil.getMethod(craftItemStack, "asBukkitCopy", itemStack);

    private static final Object ChatType_SYSTEM = ReflectionUtil.getEnumValue(chatMessageType, "SYSTEM");
    private static final Object ChatType_ACTION_BAR = ReflectionUtil.getEnumValue(chatMessageType, "GAME_INFO");

    public GameProfile getGameProfile(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (GameProfile) ReflectionUtil.callMethod(craftp, getProfile, false);

    }

    public void sendMessage(Player player, MComponent comp) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        Object nmsPl = ReflectionUtil.callMethod(craftp, getHandle, false);

        Object message = ReflectionUtil.callMethod(chatSerializer, fromJson, false, MComponent.Serializer.toJsonString(comp));

        ReflectionUtil.callMethod(nmsPl, sendMessage, false, message, ChatType_SYSTEM, nullUid);

    }

    @Override
    public void sendActionBar(Player pl, MActionBar ab) {

        Object craftp = ReflectionUtil.castTo(pl, craftPlayer);
        Object nmsPl = ReflectionUtil.callMethod(craftp, getHandle, false);

        Object message = ReflectionUtil.callMethod(chatSerializer, fromJson, false, MComponent.Serializer.toJsonString(ab.getText()));

        ReflectionUtil.callMethod(nmsPl, sendMessage, false, message, ChatType_ACTION_BAR, nullUid);
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

        return JsonConfigProvider.INSTANCE.loadFromString((String) ReflectionUtil.callMethod(compound, asString, false));
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
