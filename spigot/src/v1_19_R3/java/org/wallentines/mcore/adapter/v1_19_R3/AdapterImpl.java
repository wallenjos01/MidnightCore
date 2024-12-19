package org.wallentines.mcore.adapter.v1_19_R3;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.Objects;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private ItemReflector<net.minecraft.world.item.ItemStack, CraftItemStack> reflector;
    private Reflector<ScoreboardObjective, Objective> obReflector;
    private Reflector<ScoreboardTeam, Team> teamReflector;

    @Override
    public boolean initialize() {

        try {
            reflector = new ItemReflector<>(CraftItemStack.class);
            obReflector = new Reflector<>(Objective.class, "org.bukkit.craftbukkit.v1_19_R3.scoreboard.CraftObjective", "objective");
            teamReflector = new Reflector<>(Team.class, "org.bukkit.craftbukkit.v1_19_R3.scoreboard.CraftTeam", "team");

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
        return ep.k(i);
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

        EntityPlayer epl = ((CraftPlayer) player).getHandle();
        epl.a(convert(configSection));
        epl.c.ac().e(epl);
        for (MobEffect mobeffect : epl.el()) {
            epl.b.a(new PacketPlayOutEntityEffect(epl.af(), mobeffect));
        }
    }

    @Override
    public ItemStack buildItem(Identifier id, int count, byte data) {
        net.minecraft.world.item.ItemStack is = new net.minecraft.world.item.ItemStack(BuiltInRegistries.i.a(MinecraftKey.a(id.toString())), count);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public Identifier getItemId(ItemStack is) {
        return Identifier.parse(Objects.requireNonNull(BuiltInRegistries.i.b(reflector.getHandle(is).c())).toString());
    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection configSection) {

        reflector.getHandle(itemStack).c(configSection == null ? null : convert(configSection));
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {

        return convert(reflector.getHandle(itemStack).v());
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
        Integer clr = reflector.getHandle(itemStack).B().e.f();
        return clr == null ? Color.WHITE : new Color(clr);
    }

    @Override
    public void setObjectiveName(Objective objective, Component component) {
        obReflector.getHandle(objective).a(convert(component));
    }

    @Override
    public void setTeamPrefix(Team team, Component component) {
        teamReflector.getHandle(team).b(convert(component));
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        return NbtContext.fromMojang(
                (tag, os) -> NBTCompressedStreamTools.a(tag, (DataOutput) new DataOutputStream(os)), internal);
    }

    private NBTTagCompound convert(ConfigSection section) {
        return NbtContext.toMojang(
                section,
                is -> NBTCompressedStreamTools.a((DataInput) new DataInputStream(is)));
    }

    private IChatBaseComponent convert(Component component) {

        SerializeResult<JsonElement> serialized = ModernSerializer.INSTANCE.serialize(GameVersion.context(GsonContext.INSTANCE, getGameVersion()), component);
        if(!serialized.isComplete()) {
            MidnightCoreAPI.LOGGER.error("An error occurred while serializing a component! " + serialized.getError());
            return null;
        }
        return IChatBaseComponent.ChatSerializer.a(serialized.getOrThrow()); // fromJsonTree()
    }
}
