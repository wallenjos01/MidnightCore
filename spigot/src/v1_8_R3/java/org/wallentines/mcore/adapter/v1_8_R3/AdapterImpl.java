package org.wallentines.mcore.adapter.v1_8_R3;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.ItemReflector;
import org.wallentines.mcore.adapter.SkinUpdater;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.SNBTCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private ItemReflector<net.minecraft.server.v1_8_R3.ItemStack, CraftItemStack> reflector;
    private SNBTCodec codec;

    @Override
    public boolean initialize() {

        try {
            reflector = new ItemReflector<>(CraftItemStack.class);

        } catch (Exception ex) {
            return false;
        }

        updater = new SkinUpdaterImpl();
        codec = new SNBTCodec()
                .expectArrayIndices()
                .useDoubleQuotes();

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

        EntityPlayer epl = ((CraftPlayer) player).getHandle();
        epl.a(convert(configSection));
        epl.server.getPlayerList().updateClient(epl);
        for (MobEffect mobeffect : epl.getEffects()) {
            epl.playerConnection.sendPacket(new PacketPlayOutEntityEffect(epl.getId(), mobeffect));
        }
    }

    @Override
    public ItemStack buildItem(Identifier id, int count, byte data) {
        net.minecraft.server.v1_8_R3.ItemStack is = new net.minecraft.server.v1_8_R3.ItemStack(Item.REGISTRY.get(new MinecraftKey(id.toString())), count, data);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public Identifier getItemId(ItemStack is) {
        return Identifier.parse(Item.REGISTRY.c(reflector.getHandle(is).getItem()).toString());
    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection configSection) {
        reflector.getHandle(itemStack).setTag(convert(configSection));
    }

    @Override
    public String getTranslationKey(ItemStack is) {
        net.minecraft.server.v1_8_R3.ItemStack mis = reflector.getHandle(is);
        if(mis == null) return "";
        return mis.getItem().a(mis);
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {

        net.minecraft.server.v1_8_R3.ItemStack mis = reflector.getHandle(itemStack);
        if(mis == null) return null;

        NBTTagCompound nbt = mis.getTag();
        if(nbt == null) return null;

        return convert(nbt);
    }

    @Override
    public String getLocale(Player player) {
        return ((CraftPlayer) player).getHandle().locale;
    }

    @Override
    public ItemStack setupInternal(ItemStack itemStack) {
        return CraftItemStack.asCraftCopy(itemStack);
    }

    @Override
    public GameVersion getGameVersion() {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        return new GameVersion(server.getVersion(), 47);
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        ((CraftPlayer) player).getHandle().playerConnection.a(convert(message));
    }

    @Override
    public Color getRarityColor(ItemStack itemStack) {
        net.minecraft.server.v1_8_R3.ItemStack mis = reflector.getHandle(itemStack);
        if(mis == null) return Color.WHITE;
        return Color.fromRGBI(mis.u().e.b());
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        return codec.decode(ConfigContext.INSTANCE, internal.toString()).asSection();

    }

    private NBTTagCompound convert(ConfigSection section) {
        if(section == null) return null;
        try {
            return MojangsonParser.parse(codec.encodeToString(ConfigContext.INSTANCE, section));
        } catch (MojangsonParseException ex) { throw new RuntimeException(ex); }
    }

    private IChatBaseComponent convert(Component component) {
        return IChatBaseComponent.ChatSerializer.a(component.toJSONString(getGameVersion()));
    }
}
