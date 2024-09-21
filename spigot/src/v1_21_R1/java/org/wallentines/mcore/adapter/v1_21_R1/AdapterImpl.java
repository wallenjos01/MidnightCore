package org.wallentines.mcore.adapter.v1_21_R1;

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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R1.CraftServer;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.CustomScoreboard;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.*;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.ModernSerializer;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.GsonContext;
import org.wallentines.mdcfg.serializer.SerializeResult;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Objects;
import java.util.stream.Stream;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;
    private ItemReflector<net.minecraft.world.item.ItemStack, CraftItemStack> reflector;
    private Reflector<ScoreboardObjective, Objective> obReflector;
    private Reflector<ScoreboardTeam, Team> teamReflector;

    @Override
    public boolean initialize() {

        reflector = new ItemReflector<>(CraftItemStack.class);
        obReflector = new Reflector<>(Objective.class, "org.bukkit.craftbukkit.v1_21_R1.scoreboard.CraftObjective", "objective");
        teamReflector = new Reflector<>(Team.class, "org.bukkit.craftbukkit.v1_21_R1.scoreboard.CraftTeam", "team");

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
        return ep.l(i);
    }

    @Override
    public ConfigSection getTag(Player player) {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        ep.saveWithoutId(nbt, true);
        return convert(nbt);
    }

    @Override
    public void loadTag(Player player, ConfigSection configSection) {

        EntityPlayer epl = ((CraftPlayer) player).getHandle();
        epl.a(convert(configSection));
        epl.d.ah().e(epl);
        for (MobEffect mobeffect : epl.et()) {
            epl.c.a(new PacketPlayOutEntityEffect(epl.an(), mobeffect, false));
        }
    }

    @Override
    public ItemStack buildItem(Identifier id, int count, byte data) {
        net.minecraft.world.item.ItemStack is = new net.minecraft.world.item.ItemStack(BuiltInRegistries.g.a(MinecraftKey.a(id.toString())), count);
        return CraftItemStack.asCraftMirror(is);
    }

    @Override
    public Identifier getItemId(ItemStack is) {
        return Identifier.parse(Objects.requireNonNull(BuiltInRegistries.g.b(reflector.getHandle(is).g())).toString());
    }

    @Override
    public @Nullable ConfigObject saveComponent(ItemStack is, Identifier component) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        DataComponentType<?> type = BuiltInRegistries.au.a(MinecraftKey.a(component.getNamespace(), component.getPath())); // DATA_COMPONENT_TYPE, get
        if(type == null) {
            MidnightCoreAPI.LOGGER.warn("Unknown component type to save: {}!", component);
            return null;
        }

        // getComponents, getTyped
        TypedDataComponent<?> comp = item.a().c(type);
        if(comp == null) {
            return null;
        }

        // encodeValue
        return GsonContext.INSTANCE.convert(ConfigContext.INSTANCE, comp.a(JsonOps.INSTANCE).getOrThrow());
    }

    @Override
    public void loadComponent(ItemStack is, Identifier component, ConfigObject value) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        DataComponentType<?> type = BuiltInRegistries.au.a(MinecraftKey.a(component.getNamespace(), component.getPath())); // DATA_COMPONENT_TYPE, get
        if(type == null) {
            MidnightCoreAPI.LOGGER.warn("Unknown component type to load: {}!", component);
            return;
        }

        JsonElement json = ConfigContext.INSTANCE.convert(GsonContext.INSTANCE, value);
        loadComponent(item, type, json);
    }

    private <T> void loadComponent(net.minecraft.world.item.ItemStack item, DataComponentType<T> type, JsonElement element) {

        // set, codecOrThrow
        item.b(type, type.c().decode(JsonOps.INSTANCE, element).getOrThrow().getFirst());
    }

    @Override
    public void removeComponent(ItemStack is, Identifier component) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        DataComponentType<?> type = BuiltInRegistries.au.a(MinecraftKey.a(component.getNamespace(), component.getPath())); // DATA_COMPONENT_TYPE, get

        // applyComponents, builder, remove, build
        item.a(DataComponentPatch.a().a(type).a());
    }

    @Override
    public Stream<Identifier> getComponentIds(ItemStack is) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        return item.a().c().map(typed -> {

            MinecraftKey key = BuiltInRegistries.au.b(typed.a()); // DATA_COMPONENT_TYPE, getKey
            if(key == null) throw new IllegalStateException("Found unregistered component " + typed);

            return new Identifier(key.b(), key.a());
        });
    }

    @Override
    public org.wallentines.mcore.ItemStack.ComponentPatchSet getComponentPatch(ItemStack is) {

        net.minecraft.world.item.ItemStack item = reflector.getHandle(is);
        org.wallentines.mcore.ItemStack.ComponentPatchSet out = new org.wallentines.mcore.ItemStack.ComponentPatchSet();

        DataComponentPatch.c res = item.d().e();
        for(DataComponentType<?> type : res.a().b()) {

            MinecraftKey id = BuiltInRegistries.au.b(type);
            if(id == null) {
                MidnightCoreAPI.LOGGER.warn("Found unregistered added component {}!", type);
                continue;
            }

            TypedDataComponent<?> typed = res.a().c(type);
            out.set(new Identifier(id.b(), id.a()), GsonContext.INSTANCE.convert(ConfigContext.INSTANCE, typed.a(JsonOps.INSTANCE).getOrThrow()));
        }
        for(DataComponentType<?> type : res.b()) {

            MinecraftKey id = BuiltInRegistries.au.b(type);
            if(id == null) {
                MidnightCoreAPI.LOGGER.warn("Found unregistered removed component {}!", type);
                continue;
            }

            out.remove(new Identifier(id.b(), id.a()));
        }
        return out;
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
        Integer clr = reflector.getHandle(itemStack).y().a().f();
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

    @Override
    public void setNumberFormat(Objective objective, CustomScoreboard.NumberFormatType type, @Nullable Component argument) {

        ScoreboardObjective o = obReflector.getHandle(objective);

        switch (type) {
            case DEFAULT -> o.a((NumberFormat) null);
            case BLANK -> o.a(BlankFormat.a);
            case STYLED -> o.a(new StyledFormat(convert(argument.baseCopy()).a()));
            case FIXED -> o.a(new FixedFormat(convert(argument)));
        }
    }

    @Override
    public void setNumberFormat(Objective objective, CustomScoreboard.NumberFormatType type, @Nullable Component argument, String name) {

        ScoreboardObjective o = obReflector.getHandle(objective);

        ScoreHolder sh = ScoreHolder.c(name);
        ScoreAccess acc = o.a().c(sh, o);

        switch (type) {
            case DEFAULT -> acc.a((NumberFormat) null);
            case BLANK -> acc.a(BlankFormat.a);
            case STYLED -> acc.a(new StyledFormat(convert(argument.baseCopy()).a()));
            case FIXED -> acc.a(new FixedFormat(convert(argument)));
        }
    }

    private ConfigSection convert(NBTTagCompound internal) {
        if(internal == null) return null;
        return NbtContext.fromMojang(
                (tag, os) -> NBTCompressedStreamTools.b(tag, new DataOutputStream(os)), internal);
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
        return IChatBaseComponent.ChatSerializer.a(serialized.getOrThrow(), VanillaRegistries.a()); // fromJsonTree()
    }
}
