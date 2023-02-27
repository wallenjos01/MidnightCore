package org.wallentines.midnightcore.spigot.adapter;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.mdcfg.ConfigSection;

import java.lang.reflect.Field;

public class Adapter_v1_8_R3 implements SpigotAdapter {

    private SkinUpdater_v1_8_R3 updater;

    private Field handle;
    public net.minecraft.server.v1_8_R3.ItemStack getHandle(org.bukkit.inventory.ItemStack is) {

        try {
            return (net.minecraft.server.v1_8_R3.ItemStack) handle.get(is);

        } catch (IllegalArgumentException | IllegalAccessException ex) {

            MidnightCoreAPI.getLogger().warn("Cannot get handle of " + is.getClass() + "!");
            ex.printStackTrace();

            return CraftItemStack.asNMSCopy(is);
        }
    }

    @Override
    public ItemStack getItemInMainHand(Player pl) {
        return pl.getItemInHand();
    }

    @Override
    public ItemStack getItemInOffHand(Player pl) {
        return new ItemStack(Material.AIR);
    }

    @Override
    public boolean init() {

        try {
            handle = CraftItemStack.class.getDeclaredField("handle");
            handle.setAccessible(true);
        } catch (Exception ex) {
            return false;
        }

        updater = new SkinUpdater_v1_8_R3();
        return updater.init();
    }

    @Override
    public GameProfile getGameProfile(Player pl) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        return epl.getProfile();
    }

    @Override
    public void sendMessage(Player pl, MComponent comp) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(comp.toJSONString()), (byte) 1));
    }

    @Override
    public void sendActionBar(Player pl, MComponent comp) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.playerConnection.sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(comp.toJSONString()), (byte) 2));
    }

    @Override
    public void sendTitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a(comp.toJSONString())));
        epl.playerConnection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
    }

    @Override
    public void sendSubtitle(Player pl, MComponent comp, int fadeIn, int stay, int fadeOut) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(comp.toJSONString())));
        epl.playerConnection.sendPacket(new PacketPlayOutTitle(fadeIn, stay, fadeOut));
    }

    @Override
    public void clearTitles(Player pl) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        epl.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, null));
    }

    @Override
    public void setTag(ItemStack is, ConfigSection sec) {

        net.minecraft.server.v1_8_R3.ItemStack mis = getHandle(is);
        try {

            NBTTagCompound cmp = MojangsonParser.parse(MItemStack.toNBT(sec));
            mis.setTag(cmp);

        } catch (MojangsonParseException ex) {

            MidnightCoreAPI.getLogger().warn("Unable to change item tag!");
        }
    }

    @Override
    public ConfigSection getTag(ItemStack is) {

        net.minecraft.server.v1_8_R3.ItemStack mis = getHandle(is);
        NBTTagCompound cmp = mis.getTag();

        if(cmp == null) return null;
        return JSONCodec.loadConfig(cmp.toString()).asSection();
    }

    @Override
    public boolean hasOpLevel(Player pl, int lvl) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        return epl.a(lvl, "");
    }

    @Override
    public ConfigSection getTag(Player pl) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        epl.b(tag);

        return JSONCodec.loadConfig(tag.toString()).asSection();
    }

    @Override
    public void loadTag(Player pl, ConfigSection tag) {

        EntityPlayer epl = ((CraftPlayer) pl).getHandle();

        try {
            NBTTagCompound nbt = MojangsonParser.parse(MItemStack.toNBT(tag));
            epl.a(nbt);

        } catch (MojangsonParseException ex) {
            MidnightCoreAPI.getLogger().warn("Unable to load player data from tag!");
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

    @Override
    public void addTickable(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().getServer().a(runnable::run);
    }
}
