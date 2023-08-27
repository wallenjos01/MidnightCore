package org.wallentines.mcore.adapter.v1_19_R1v2;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.dewy.nbt.tags.collection.CompoundTag;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
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
import org.wallentines.mcore.util.ItemUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.io.DataInput;
import java.lang.reflect.Field;
import java.util.List;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private Field handle;

    public net.minecraft.world.item.ItemStack getHandle(ItemStack is) {
        try {
            return (net.minecraft.world.item.ItemStack) handle.get(is);
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
        ((CraftServer) Bukkit.getServer()).getServer().g(runnable);
    }

    @Override
    public void addTickListener(Runnable runnable) {
        ((CraftServer) Bukkit.getServer()).getServer().b(runnable);
    }

    @Override
    public @Nullable Skin getPlayerSkin(Player player) {
        GameProfile profile = ((CraftPlayer) player).getProfile();
        return profile.getProperties().get("textures").stream().map(prop -> new Skin(profile.getId(), prop.getValue(), prop.getSignature())).findFirst().orElse(null);
    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return updater;
    }

    @Override
    public void sendMessage(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.a(bc, false); // sendMessage()
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.a(bc, true); // sendMessage()
    }

    @Override
    public void sendTitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.b.a(new ClientboundSetTitleTextPacket(bc));
    }

    @Override
    public void sendSubtitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.b.a(new ClientboundSetSubtitleTextPacket(bc));
    }

    @Override
    public void setTitleAnimation(Player player, int i, int i1, int i2) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.b.a(new ClientboundSetTitlesAnimationPacket(i, i1, i2));
    }

    @Override
    public void clearTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.b.a(new ClientboundClearTitlesPacket(false));
    }

    @Override
    public void resetTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.b.a(new ClientboundClearTitlesPacket(true));
    }

    @Override
    public boolean hasOpLevel(Player player, int i) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        return ep.l(i);
    }

    @Override
    public ConfigSection getTag(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        ep.f(nbt);
        return convert(nbt);
    }

    @Override
    public void loadTag(Player player, ConfigSection configSection) {
        ((CraftPlayer) player).getHandle().a(convert(configSection));
    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection configSection) {
        getHandle(itemStack).c(convert(configSection));
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {

        net.minecraft.world.item.ItemStack mis = getHandle(itemStack);
        NBTTagCompound nbt = mis.u();
        if(nbt == null) return null;

        return convert(nbt);
    }

    @Override
    public ItemStack setupInternal(ItemStack itemStack) {
        return CraftItemStack.asCraftCopy(itemStack);
    }

    @Override
    public GameVersion getGameVersion() {
        return new GameVersion(SharedConstants.b().getId(), SharedConstants.b().getProtocolVersion());
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        ((CraftPlayer) player).getHandle().b.a(convert(message));
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        CompoundTag converted = NbtContext.fromMojang(NBTCompressedStreamTools::a, internal);
        return NbtContext.INSTANCE.convert(ConfigContext.INSTANCE, converted).asSection();
    }

    private NBTTagCompound convert(ConfigSection section) {
        return NbtContext.toMojang(
                (CompoundTag) ConfigContext.INSTANCE.convert(NbtContext.INSTANCE, section),
                dis -> NBTCompressedStreamTools.a((DataInput) dis));
    }

    private IChatBaseComponent convert(Component component) {

        SerializeResult<JsonElement> serialized = ModernSerializer.INSTANCE.serialize(GsonContext.INSTANCE, component);
        if(!serialized.isComplete()) {
            MidnightCoreAPI.LOGGER.error("An error occurred while serializing a component! " + serialized.getError());
            return null;
        }
        return IChatBaseComponent.ChatSerializer.a(serialized.getOrThrow()); // fromJsonTree()
    }
}
