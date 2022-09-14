package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.config.serialization.json.JsonConfigProvider;

import java.lang.reflect.Field;

public class Adapter_v1_16_R3 implements SpigotAdapter {

    private SkinUpdater_v1_16_R3 updater;

    private Field handle;

    public net.minecraft.server.v1_16_R3.ItemStack getHandle(org.bukkit.inventory.ItemStack is) {

        try {
            return (net.minecraft.server.v1_16_R3.ItemStack) handle.get(is);

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

        updater = new SkinUpdater_v1_16_R3();
        return updater.init();
    }

    @Override
    public GameProfile getGameProfile(Player pl) {
        return null;
    }

    @Override
    public void sendMessage(Player pl, MComponent comp) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.a(IChatBaseComponent.ChatSerializer.a(toJsonString(comp)), ChatMessageType.SYSTEM, NIL_UUID);

    }

    @Override
    public void sendActionBar(Player pl, MComponent comp) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.a(IChatBaseComponent.ChatSerializer.a(toJsonString(comp)), ChatMessageType.GAME_INFO, NIL_UUID);

    }

    @Override
    public void sendTitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();

        epl.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a(toJsonString(comp)), fadeIn, stay, fadeOut));
    }

    @Override
    public void sendSubtitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();

        epl.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(toJsonString(comp)), fadeIn, stay, fadeOut));
    }

    @Override
    public void clearTitles(Player pl) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();

        epl.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, null));
    }

    @Override
    public void setTag(ItemStack is, ConfigSection sec) {

        net.minecraft.server.v1_16_R3.ItemStack mis = getHandle(is);

        try {
            NBTTagCompound cmp = MojangsonParser.parse(MItemStack.toNBT(sec));
            mis.setTag(cmp);

        } catch (CommandSyntaxException ex) {
            // Ignore
        }
    }

    @Override
    public ConfigSection getTag(ItemStack is) {

        net.minecraft.server.v1_16_R3.ItemStack mis = getHandle(is);

        NBTTagCompound cmp = mis.getTag();
        if (cmp == null) return null;

        return JsonConfigProvider.INSTANCE.loadFromString(cmp.asString());
    }

    @Override
    public boolean hasOpLevel(Player pl, int lvl) {
        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        return epl.k(lvl);
    }

    @Override
    public ConfigSection getTag(Player pl) {
        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        tag = epl.save(tag);

        return JsonConfigProvider.INSTANCE.loadFromString(tag.asString());
    }

    @Override
    public void loadTag(Player pl, ConfigSection tag) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        try {
            NBTTagCompound nbt = MojangsonParser.parse(MItemStack.toNBT(tag));
            epl.load(nbt);

        } catch (CommandSyntaxException ex) {
            // Ignore
        }
    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return updater;
    }

    @Override
    public ItemStack setupInternal(ItemStack item) {
        return CraftItemStack.asCraftCopy(item);
    }

}

