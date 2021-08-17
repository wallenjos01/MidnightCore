package me.m1dnightninja.midnightcore.spigot.version.v1_13;

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

public class NMSUtil_13_15 implements NMSUtil.NMSHandler {

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
    private static final Method sendMessage = ReflectionUtil.getMethod(serverPlayer, "a", baseComponent, chatMessageType);
    private static final Method fromJson = ReflectionUtil.getMethod(chatSerializer, "a", String.class);
    private static final Method setTag = ReflectionUtil.getMethod(itemStack, "setTag", nbtTagCompound);
    private static final Method getTag = ReflectionUtil.getMethod(itemStack, "getTag");
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

        ReflectionUtil.callMethod(nmsPl, sendMessage, false, message, ChatType_SYSTEM);

    }

    @Override
    public void sendActionBar(Player pl, MActionBar ab) {

        Object craftp = ReflectionUtil.castTo(pl, craftPlayer);
        Object nmsPl = ReflectionUtil.callMethod(craftp, getHandle, false);

        Object message = ReflectionUtil.callMethod(chatSerializer, fromJson, false, MComponent.Serializer.toJsonString(ab.getText()));

        ReflectionUtil.callMethod(nmsPl, sendMessage, false, message, ChatType_ACTION_BAR);
    }

    @Override
    public void sendTitle(Player pl, MTitle title) {

        String text = MComponent.Serializer.toLegacyText(title.getText());

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

        if(is == null) return null;
        Object mis = ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, is);

        if(mis == null) return new ConfigSection();
        Object compound = ReflectionUtil.callMethod(mis, getTag, false);

        if(compound == null) return new ConfigSection();

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
