package me.m1dnightninja.midnightcore.spigot.version.v1_17;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.common.config.JsonConfigProvider;
import me.m1dnightninja.midnightcore.spigot.util.NMSUtil;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.UUID;

public class NMSUtil_17 implements NMSUtil.NMSHandler {

    public static final UUID nullUid = new UUID(0L, 0L);

    private static final Class<?> craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
    private static final Class<?> craftItemStack = ReflectionUtil.getCraftBukkitClass("inventory.CraftItemStack");

    private static final Method getHandle = ReflectionUtil.getMethod(craftPlayer, "getHandle");
    private static final Method getProfile = ReflectionUtil.getMethod(craftPlayer, "getProfile");
    private static final Method asNMSCopy = ReflectionUtil.getMethod(craftItemStack, "asNMSCopy", ItemStack.class);
    private static final Method asBukkitCopy = ReflectionUtil.getMethod(craftItemStack, "asBukkitCopy", net.minecraft.world.item.ItemStack.class);

    private EntityPlayer toEntityPlayer(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (EntityPlayer) ReflectionUtil.callMethod(craftp, getHandle, false);
    }

    public GameProfile getGameProfile(Player player) {

        Object craftp = ReflectionUtil.castTo(player, craftPlayer);
        return (GameProfile) ReflectionUtil.callMethod(craftp, getProfile, false);

    }

    public void sendMessage(Player player, MComponent comp) {

        EntityPlayer nmsPl = toEntityPlayer(player);

        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(MComponent.Serializer.toJson(comp, true));
        nmsPl.sendMessage(message, nullUid);

    }

    @Override
    public void sendActionBar(Player pl, MActionBar ab) {

        EntityPlayer nmsPl = toEntityPlayer(pl);
        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(MComponent.Serializer.toJson(ab.getText(), true));

        nmsPl.a(message, true);
    }

    @Override
    public void sendTitle(Player pl, MTitle title) {

        EntityPlayer nmsPl = toEntityPlayer(pl);

        if(title.getOptions().clear) {
            nmsPl.b.sendPacket(new ClientboundClearTitlesPacket(true));
        }

        IChatBaseComponent message = IChatBaseComponent.ChatSerializer.a(MComponent.Serializer.toJson(title.getText(), true));

        if(title.getOptions().subtitle) {
            nmsPl.b.sendPacket(new ClientboundSetSubtitleTextPacket(message));
        } else {
            nmsPl.b.sendPacket(new ClientboundSetTitleTextPacket(message));
        }

        nmsPl.b.sendPacket(new ClientboundSetTitlesAnimationPacket(title.getOptions().fadeIn, title.getOptions().stay, title.getOptions().fadeOut));
    }

    @Override
    public ConfigSection getItemTag(ItemStack is) {

        net.minecraft.world.item.ItemStack mis = (net.minecraft.world.item.ItemStack) ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, is);
        NBTTagCompound compound = mis.getTag();

        if(compound == null) return new ConfigSection();
        return JsonConfigProvider.INSTANCE.loadFromString(compound.asString());
    }

    @Override
    public ItemStack setItemTag(ItemStack is, ConfigSection tag) {

        net.minecraft.world.item.ItemStack mis = (net.minecraft.world.item.ItemStack) ReflectionUtil.callMethod(craftItemStack, asNMSCopy, false, is);
        String json = tag.toNBT();

        try {
            NBTTagCompound cmp = MojangsonParser.parse(json);
            mis.setTag(cmp);

            return (ItemStack) ReflectionUtil.callMethod(craftItemStack, asBukkitCopy, false, mis);

        } catch(CommandSyntaxException ex) {
            // Ignore
        }

        return is;
    }
}
