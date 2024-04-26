package org.wallentines.mcore.adapter.v1_20_R4;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.ItemReflector;
import org.wallentines.mcore.adapter.NbtContext;
import org.wallentines.mcore.adapter.SkinUpdater;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Objects;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private ItemReflector<net.minecraft.world.item.ItemStack, CraftItemStack> reflector;

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
        server.getHandle().b().g(runnable);
    }

    @Override
    public void addTickListener(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().b().b(runnable);
    }

    @Override
    public @Nullable Skin getPlayerSkin(Player player) {

        GameProfile profile = ((CraftPlayer) player).getProfile();
        return profile.getProperties().get("textures").stream().map(prop -> new Skin(profile.getId(), prop.value(), prop.signature())).findFirst().orElse(null);
    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return updater;
    }

    @Override
    public void sendMessage(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.a(bc); // sendMessage()
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.b(bc, true); // sendMessage()
    }

    @Override
    public void sendTitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.c.a(new ClientboundSetTitleTextPacket(bc));
    }

    @Override
    public void sendSubtitle(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.c.a(new ClientboundSetSubtitleTextPacket(bc));
    }

    @Override
    public void setTitleAnimation(Player player, int i, int i1, int i2) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.c.a(new ClientboundSetTitlesAnimationPacket(i, i1, i2));
    }

    @Override
    public void clearTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.c.a(new ClientboundClearTitlesPacket(false));
    }

    @Override
    public void resetTitles(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        ep.c.a(new ClientboundClearTitlesPacket(true));
    }

    @Override
    public boolean hasOpLevel(Player player, int i) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        return ep.m(i);
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
    public ItemStack buildItem(Identifier id, int count, byte data) {
        net.minecraft.world.item.ItemStack is = new net.minecraft.world.item.ItemStack(BuiltInRegistries.h.a(MinecraftKey.a(id.toString())), count);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public Identifier getItemId(ItemStack is) {
        return Identifier.parse(Objects.requireNonNull(BuiltInRegistries.h.b(reflector.getHandle(is).g())).toString());
    }

    @Override
    public @Nullable ConfigObject saveComponent(ItemStack is, Identifier component) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        DataComponentType<?> type = BuiltInRegistries.as.a(new MinecraftKey(component.getNamespace(), component.getPath())); // DATA_COMPONENT_TYPE, get
        if(type == null) {
            MidnightCoreAPI.LOGGER.warn("Unknown component type " + component + "!");
            return null;
        }

        // getComponents, getTyped
        TypedDataComponent<?> comp = item.c().c(type);
        if(comp == null) {
            return null;
        }

        // encodeValue
        return GsonContext.INSTANCE.convert(ConfigContext.INSTANCE, comp.a(JsonOps.INSTANCE).getOrThrow());
    }

    @Override
    public void loadComponent(ItemStack is, Identifier component, ConfigObject value) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        DataComponentType<?> type = BuiltInRegistries.as.a(new MinecraftKey(component.getNamespace(), component.getPath())); // DATA_COMPONENT_TYPE, get
        if(type == null) {
            MidnightCoreAPI.LOGGER.warn("Unknown component type " + component + "!");
            return;
        }
        // set, codecOrThrow
        item.a(type, type.c().decode(JsonOps.INSTANCE, ConfigContext.INSTANCE.convert(GsonContext.INSTANCE, value)).getOrThrow().getFirst());
    }

    @Override
    public void removeComponent(ItemStack is, Identifier component) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        DataComponentType<?> type = BuiltInRegistries.as.a(new MinecraftKey(component.getNamespace(), component.getPath())); // DATA_COMPONENT_TYPE, get

        // applyComponents, builder, remove, build
        item.a(DataComponentPatch.a().a(type).a());
    }

    @Override
    public ItemStack setupInternal(ItemStack itemStack) {
        return CraftItemStack.asCraftCopy(itemStack);
    }

    @Override
    public GameVersion getGameVersion() {
        return new GameVersion(SharedConstants.b().c(), SharedConstants.c()); // getCurrentVersion, getId, getProtocolVersion
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        ((CraftPlayer) player).getHandle().c.a(convert(message));
    }

    @Override
    public Color getRarityColor(ItemStack itemStack) {
        Integer clr = reflector.getHandle(itemStack).z().a().f();
        return clr == null ? Color.WHITE : new Color(clr);
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        return NbtContext.fromMojang(NBTCompressedStreamTools::a, internal);
    }

    private NBTTagCompound convert(ConfigSection section) {
        return NbtContext.toMojang(
                section,
                is -> NBTCompressedStreamTools.a(is, NBTReadLimiter.a()));
    }

    private IChatBaseComponent convert(Component component) {

        SerializeResult<JsonElement> serialized = ModernSerializer.INSTANCE.serialize(GsonContext.INSTANCE, component, getGameVersion());
        if(!serialized.isComplete()) {
            MidnightCoreAPI.LOGGER.error("An error occurred while serializing a component! " + serialized.getError());
            return null;
        }
        return IChatBaseComponent.ChatSerializer.a(serialized.getOrThrow(), VanillaRegistries.a()); // fromJsonTree()
    }
}