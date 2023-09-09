package org.wallentines.mcore.adapter.v1_13_R2v2;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.dewy.nbt.tags.collection.CompoundTag;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.NbtContext;
import org.wallentines.mcore.adapter.SkinUpdater;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.lang.reflect.Field;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private Field handle;
    public net.minecraft.server.v1_13_R2.ItemStack getHandle(ItemStack is) {

        try {
            return (net.minecraft.server.v1_13_R2.ItemStack) handle.get(is);

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
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutChat(bc, ChatMessageType.SYSTEM));
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutChat(bc, ChatMessageType.GAME_INFO));
    }

    @Override
    public void sendTitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, bc));
    }

    @Override
    public void sendSubtitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, bc));
    }

    @Override
    public void setTitleAnimation(Player player, int i, int i1, int i2) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, i, i1, i2));
    }

    @Override
    public void clearTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.CLEAR, null));
    }

    @Override
    public void resetTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.playerConnection.sendPacket(new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.RESET, null));
    }

    @Override
    public boolean hasOpLevel(Player player, int i) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        return ep.j(i); // hasPermissions
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
        ((CraftPlayer) player).getHandle().a(convert(configSection));
    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection configSection) {
        getHandle(itemStack).setTag(convert(configSection));
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {

        net.minecraft.server.v1_13_R2.ItemStack mis = getHandle(itemStack);
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
        ServerPing.ServerData data = ((CraftServer) Bukkit.getServer()).getServer().getServerPing().getServerData();
        return new GameVersion(data.a(), data.getProtocolVersion());
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        ((CraftPlayer) player).getHandle().playerConnection.a(convert(message));
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        CompoundTag converted = NbtContext.fromMojang(NBTCompressedStreamTools::a, internal);
        return NbtContext.INSTANCE.convert(ConfigContext.INSTANCE, converted).asSection();
    }

    private NBTTagCompound convert(ConfigSection section) {
        return NbtContext.toMojang(
                (CompoundTag) ConfigContext.INSTANCE.convert(NbtContext.INSTANCE, section),
                NBTCompressedStreamTools::a);
    }

    private IChatBaseComponent convert(Component component) {
        SerializeResult<JsonElement> serialized = ModernSerializer.INSTANCE.serialize(GsonContext.INSTANCE, component);
        if(!serialized.isComplete()) {
            MidnightCoreAPI.LOGGER.error("An error occurred while serializing a component! " + serialized.getError());
            return null;
        }
        return IChatBaseComponent.ChatSerializer.a(serialized.getOrThrow());
    }
}
