package org.wallentines.mcore.adapter.v1_13_R1;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_13_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.*;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.Objects;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private ItemReflector<net.minecraft.server.v1_13_R1.ItemStack, CraftItemStack> reflector;

    @Override
    public boolean initialize() {

        try {
            reflector = new ItemReflector<>(CraftItemStack.class);

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
        return new Skin(profile.getId(), property.getValue(), property.getSignature());
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
        return ep.k(i); // hasPermissions
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
    public ItemStack buildItem(Identifier id, int count, byte data) {
        net.minecraft.server.v1_13_R1.ItemStack is = new net.minecraft.server.v1_13_R1.ItemStack(Item.REGISTRY.get(new MinecraftKey(id.toString())), count);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public Identifier getItemId(ItemStack is) {
        return Identifier.parse(Objects.requireNonNull(Item.REGISTRY.b(reflector.getHandle(is).getItem())).toString());
    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection configSection) {
        reflector.getHandle(itemStack).setTag(convert(configSection));
    }

    @Override
    public String getTranslationKey(ItemStack is) {
        net.minecraft.server.v1_13_R1.ItemStack mis = reflector.getHandle(is);
        return mis.getItem().getName();
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {

        net.minecraft.server.v1_13_R1.ItemStack mis = reflector.getHandle(itemStack);
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
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        return new UncertainGameVersion<>(server.getVersion(), 393,
                () -> ((CraftServer) Bukkit.getServer()).getServer().getServerPing().getServerData(),
                ServerPing.ServerData::getProtocolVersion
        );
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        ((CraftPlayer) player).getHandle().playerConnection.a(convert(message));
    }

    @Override
    public Color getRarityColor(ItemStack itemStack) {
        return Color.fromRGBI(reflector.getHandle(itemStack).u().e.b());
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        return NbtContext.fromMojang(
                (tag, os) -> NBTCompressedStreamTools.a(tag, (DataOutput) new DataOutputStream(os)), internal);
    }

    private NBTTagCompound convert(ConfigSection section) {
        return NbtContext.toMojang(
                section,
                is -> NBTCompressedStreamTools.a(new DataInputStream(is)));
    }

    private IChatBaseComponent convert(Component component) {
        SerializeResult<JsonElement> serialized = ModernSerializer.INSTANCE.serialize(GsonContext.INSTANCE, component, getGameVersion());
        if(!serialized.isComplete()) {
            MidnightCoreAPI.LOGGER.error("An error occurred while serializing a component! " + serialized.getError());
            return null;
        }
        return IChatBaseComponent.ChatSerializer.a(serialized.getOrThrow());
    }
}
