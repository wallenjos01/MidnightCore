package org.wallentines.mcore.adapter.v1_8_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.SkinUpdater;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private Field handle;

    public net.minecraft.server.v1_8_R1.ItemStack getHandle(org.bukkit.inventory.ItemStack is) {

        try {
            return (net.minecraft.server.v1_8_R1.ItemStack) handle.get(is);

        } catch (Exception ex) {
            return CraftItemStack.asNMSCopy(is);
        }
    }


    
    @Override
    public boolean initialize() {

        try {
            handle = CraftItemStack.class.getDeclaredField("handle");
            handle.setAccessible(true);

        } catch (Exception ex) {
            return false;
        }

        updater = new SkinUpdaterImpl();

        return true;
    }

    @Override
    public void runOnServer(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().getServer().postToMainThread(runnable);
    }

    @Override
    public void addTickListener(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().getServer().a(runnable::run);
    }

    @Override
    public @Nullable Skin getPlayerSkin(Player player) {
        GameProfile profile = ((CraftPlayer) player).getProfile();
        if(!profile.getProperties().containsKey("textures") || profile.getProperties().get("textures").isEmpty()) { return null; }
        Property property = profile.getProperties().get("textures").iterator().next();
        return new Skin(profile.getId(), property.getValue(), property.getValue());
    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return updater;
    }

    @Override
    public void sendMessage(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutChat(bc, (byte) 1));
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutChat(bc, (byte) 2));
    }

    @Override
    public void sendTitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, bc));
    }

    @Override
    public void sendSubtitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, bc));
    }

    @Override
    public void setTitleAnimation(Player player, int i, int i1, int i2) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TIMES, null, i, i1, i2));
    }

    @Override
    public void clearTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.CLEAR, null));
    }

    @Override
    public void resetTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.RESET, null));
    }

    @Override
    public boolean hasOpLevel(Player player, int i) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        return ep.server.getPlayerList().isOp(((CraftPlayer) player).getProfile());
    }

    @Override
    public ConfigSection getTag(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        ep.b(nbt); // save()
        return convert(nbt);
    }

    @Override
    public void loadTag(Player player, ConfigSection configSection) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        try {
            NBTTagCompound nbt = MojangsonParser.parse(ItemUtil.toNBTString(ConfigContext.INSTANCE, configSection));
            ep.a(nbt); // load()
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An error occurred while loading a player tag! " + ex.getMessage());
        }
    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection configSection) {

        net.minecraft.server.v1_8_R1.ItemStack mis = getHandle(itemStack);
        try {
            NBTTagCompound nbt = MojangsonParser.parse(ItemUtil.toNBTString(ConfigContext.INSTANCE, configSection));
            mis.setTag(nbt);
        } catch (Exception ex) {
            MidnightCoreAPI.LOGGER.error("An error occurred while changing an item tag! " + ex.getMessage());
        }
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {

        net.minecraft.server.v1_8_R1.ItemStack mis = getHandle(itemStack);
        NBTTagCompound nbt = mis.getTag();
        if(nbt == null) return null;

        return convert(nbt);
    }

    @Override
    public ItemStack setupInternal(ItemStack itemStack) {
        return CraftItemStack.asCraftCopy(itemStack);
    }

    @Override
    public GameVersion getGameVersion() {
        ServerPingServerData data = MinecraftServer.getServer().aE().c(); // ServerPing, ServerPingServerData
        return new GameVersion(data.a(), data.b());
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        ((CraftPlayer) player).getHandle().playerConnection.a(convert(message));
    }
    
    private ConfigSection convert(NBTTagCompound nbt) {
        // Flatten int arrays, byte arrays, and long arrays to nbt lists
        for(Object oKey : new ArrayList<Object>(nbt.c())) {

            String key = (String) oKey;

            NBTBase base = nbt.get(key);
            if(base instanceof NBTTagList && base.getClass() != NBTTagList.class) {
                NBTTagList flattened = new NBTTagList();
                for(int i = 0 ; i < ((NBTTagList) base).size() ; i++) {
                    flattened.add(((NBTTagList) base).get(i));
                }
                nbt.set(key, flattened);
            }
        }
        return JSONCodec.loadConfig(nbt.toString()).asSection();
    }

    private IChatBaseComponent convert(Component component) {
        return ChatSerializer.a(component.toJSONString());
    }
}
