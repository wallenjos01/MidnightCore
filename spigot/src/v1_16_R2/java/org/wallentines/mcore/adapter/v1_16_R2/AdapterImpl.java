package org.wallentines.mcore.adapter.v1_16_R2;

import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.v1_16_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
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
import org.wallentines.mdcfg.codec.SNBTCodec;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Objects;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private ItemReflector<net.minecraft.server.v1_16_R2.ItemStack, CraftItemStack> reflector;
    private Reflector<ScoreboardObjective, Objective> obReflector;
    private Reflector<ScoreboardTeam, Team> teamReflector;
    private SNBTCodec codec;

    @Override
    public boolean initialize() {

        try {
            reflector = new ItemReflector<>(CraftItemStack.class);
            obReflector = new Reflector<>(Objective.class, "org.bukkit.craftbukkit.v1_16_R2.scoreboard.CraftObjective", "objective");
            teamReflector = new Reflector<>(Team.class, "org.bukkit.craftbukkit.v1_16_R2.scoreboard.CraftTeam", "team");

        } catch (Exception ex) {
            return false;
        }

        updater = new SkinUpdaterImpl();
        codec = new SNBTCodec()
                .useDoubleQuotes();

        return true;
    }

    @Override
    public void runOnServer(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().getServer().f(runnable);
    }

    @Override
    public void addTickListener(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().getServer().b(runnable);
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
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutChat(bc, ChatMessageType.SYSTEM, SystemUtils.b)); // NIL_UUID
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        IChatBaseComponent bc = convert(component);
        if(bc != null) ep.playerConnection.sendPacket(new PacketPlayOutChat(bc, ChatMessageType.GAME_INFO, SystemUtils.b));
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
        ep.save(nbt); // save()
        return convert(nbt);
    }

    @Override
    public void loadTag(Player player, ConfigSection configSection) {
        EntityPlayer epl = ((CraftPlayer) player).getHandle();
        epl.load(convert(configSection));
        epl.server.getPlayerList().updateClient(epl);
        for (MobEffect mobeffect : epl.getEffects()) {
            epl.playerConnection.sendPacket(new PacketPlayOutEntityEffect(epl.getId(), mobeffect));
        }
    }

    @Override
    public ItemStack buildItem(Identifier id, int count, byte data) {
        net.minecraft.server.v1_16_R2.ItemStack is = new net.minecraft.server.v1_16_R2.ItemStack(IRegistry.ITEM.get(new MinecraftKey(id.toString())), count);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public Identifier getItemId(ItemStack is) {
        return Identifier.parse(Objects.requireNonNull(IRegistry.ITEM.getKey(reflector.getHandle(is).getItem())).toString());
    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection configSection) {
        reflector.getHandle(itemStack).setTag(convert(configSection));
    }

    @Override
    public String getTranslationKey(ItemStack is) {
        net.minecraft.server.v1_16_R2.ItemStack mis = reflector.getHandle(is);
        return mis.getItem().getName();
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {

        net.minecraft.server.v1_16_R2.ItemStack mis = reflector.getHandle(itemStack);
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
        return new GameVersion(SharedConstants.getGameVersion().getId(), SharedConstants.getGameVersion().getProtocolVersion());
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        ((CraftPlayer) player).getHandle().playerConnection.a(convert(message));
    }

    @Override
    public Color getRarityColor(ItemStack itemStack) {
        Integer clr = reflector.getHandle(itemStack).v().e.e();
        return clr == null ? Color.WHITE : new Color(clr);
    }

    @Override
    public void setObjectiveName(Objective objective, Component component) {
        obReflector.getHandle(objective).setDisplayName(convert(component));
    }

    @Override
    public void setTeamPrefix(Team team, Component component) {
        teamReflector.getHandle(team).setPrefix(convert(component));
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        return codec.decode(ConfigContext.INSTANCE, internal.asString()).asSection();
    }

    private NBTTagCompound convert(ConfigSection section) {
        if(section == null) return null;
        try {
            return MojangsonParser.parse(codec.encodeToString(ConfigContext.INSTANCE, section));
        } catch (CommandSyntaxException ex) { throw new RuntimeException(ex); }
    }

    private IChatBaseComponent convert(Component component) {
        SerializeResult<JsonElement> serialized = ModernSerializer.INSTANCE.serialize(GameVersion.context(GsonContext.INSTANCE, getGameVersion()), component);
        if(!serialized.isComplete()) {
            MidnightCoreAPI.LOGGER.error("An error occurred while serializing a component! " + serialized.getError());
            return null;
        }
        return IChatBaseComponent.ChatSerializer.a(serialized.getOrThrow());
    }
}
