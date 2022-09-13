package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.ChatMessageType;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

import java.lang.reflect.Field;

public class Adapter_v1_18_R2 implements SpigotAdapter {

    private SkinUpdater_v1_18_R2 updater;
    private Field handle;
    public net.minecraft.world.item.ItemStack getHandle(org.bukkit.inventory.ItemStack is) {

        try {
            return (net.minecraft.world.item.ItemStack) handle.get(is);

        } catch (Exception ex) {
            return CraftItemStack.asNMSCopy(is);
        }
    }

    @Override
    public boolean init() {

        try {
            handle = CraftItemStack.class.getDeclaredField("handle");
            handle.setAccessible(true);

        } catch (Exception ex) {
            return false;
        }

        updater = new SkinUpdater_v1_18_R2();
        return updater.init();
    }

    @Override
    public GameProfile getGameProfile(Player pl) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        return epl.fq();
    }

    @Override
    public void sendMessage(Player pl, MComponent comp) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.a(IChatBaseComponent.ChatSerializer.a(comp.toString()), ChatMessageType.a, NIL_UUID);

    }

    @Override
    public void sendActionBar(Player pl, MComponent comp) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.a(IChatBaseComponent.ChatSerializer.a(comp.toString()), ChatMessageType.c, NIL_UUID);

    }

    @Override
    public void sendTitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.b.a(new ClientboundSetTitleTextPacket(IChatBaseComponent.ChatSerializer.a(comp.toString())));
        epl.b.a(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    }

    @Override
    public void sendSubtitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.b.a(new ClientboundSetSubtitleTextPacket(IChatBaseComponent.ChatSerializer.a(comp.toString())));
        epl.b.a(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
    }

    @Override
    public void clearTitles(Player pl) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.b.a(new ClientboundClearTitlesPacket(true));
    }

    @Override
    public void setTag(ItemStack is, ConfigSection sec) {

        net.minecraft.world.item.ItemStack mis = getHandle(is);

        try {
            String str = MItemStack.toNBT(sec);

            MidnightCoreAPI.getLogger().info(str);

            NBTTagCompound cmp = MojangsonParser.a(str);
            mis.c(cmp);

        } catch (CommandSyntaxException ex) {
            // Ignore
        }
    }

    @Override
    public ConfigSection getTag(ItemStack is) {

        net.minecraft.world.item.ItemStack mis = getHandle(is);

        NBTTagCompound cmp = mis.t();
        if(cmp == null) return null;

        return JsonConfigProvider.INSTANCE.loadFromString(cmp.e_());
    }

    @Override
    public boolean hasOpLevel(Player pl, int lvl) {
        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        return epl.l(lvl);
    }

    @Override
    public ConfigSection getTag(Player pl) {
        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        tag = epl.f(tag);

        return JsonConfigProvider.INSTANCE.loadFromString(tag.e_());
    }

    @Override
    public void loadTag(Player pl, ConfigSection tag) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        try {
            NBTTagCompound nbt = MojangsonParser.a(MItemStack.toNBT(tag));
            epl.a(nbt);

        } catch (CommandSyntaxException ex) {
            // Ignore
        }

    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return updater;
    }

}
