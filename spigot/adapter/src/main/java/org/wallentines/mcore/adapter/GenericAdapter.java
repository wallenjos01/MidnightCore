package org.wallentines.mcore.adapter;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.text.TextColor;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.Version;
import org.wallentines.midnightlib.math.Color;
import org.wallentines.midnightlib.registry.Identifier;

public class GenericAdapter implements Adapter {

    private final Plugin plugin;

    public GenericAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean initialize() {
        return plugin != null;
    }

    @Override
    public void runOnServer(Runnable runnable) {
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public void addTickListener(Runnable runnable) {
        Bukkit.getScheduler().runTaskTimer(plugin, runnable, 50L, 50L);
    }

    @Override
    public @Nullable Skin getPlayerSkin(Player player) {
        return null;
    }

    @Override
    public SkinUpdater getSkinUpdater() {
        return null;
    }

    @Override
    public void sendMessage(Player player, Component component) {
        player.spigot().sendMessage(ComponentSerializer.parse(component.toJSONString()));
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse(component.toJSONString()));
    }

    @Override
    public void sendTitle(Player player, Component component) {
        player.sendTitle(component.toLegacyText(), null, 10, 70, 20);
    }

    @Override
    public void sendSubtitle(Player player, Component component) {
        player.sendTitle(null, component.toLegacyText(), 10, 70, 20);
    }

    @Override
    public void setTitleAnimation(Player player, int fadeIn, int stay, int fadeOut) { }

    @Override
    public void clearTitles(Player player) {
        player.resetTitle();
    }

    @Override
    public void resetTitles(Player player) {
        player.resetTitle();
    }

    @Override
    public boolean hasOpLevel(Player player, int level) {
        return player.isOp();
    }

    @Override
    public ConfigSection getTag(Player player) {
        return null;
    }

    @Override
    public void loadTag(Player player, ConfigSection tag) {

    }

    @Override
    public ItemStack buildItem(Identifier id, int count, byte data) {
        Material mat = Material.getMaterial(id.toString());
        if(mat == null) return new ItemStack(Material.AIR);

        return new ItemStack(mat, count);
    }

    @Override
    public Identifier getItemId(ItemStack stack) {
        return null;
    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {
        return null;
    }

    @Override
    public ItemStack setupInternal(ItemStack item) {
        return item;
    }

    @Override
    public GameVersion getGameVersion() {

        String versionString = Bukkit.getVersion().substring(Bukkit.getVersion().indexOf("MC: ") + 4, Bukkit.getVersion().length() - 1);
        Version version = Version.fromString(versionString);
        if(version == null) return new GameVersion(versionString, 0);

        int protocol = 0;
        for(MinecraftVersion mc : MinecraftVersion.values()) {
            if(version.isGreaterOrEqual(mc.version)) {
                if(mc.protocolVersion > protocol) protocol = mc.protocolVersion;
            }
        }

        return new GameVersion(versionString, protocol);
    }

    @Override
    public void kickPlayer(Player player, Component message) {
        player.kickPlayer(message.toLegacyText());
    }

    @Override
    public Color getRarityColor(ItemStack itemStack) {
        if(itemStack.getItemMeta() != null && itemStack.getItemMeta().hasEnchants()) {
            return TextColor.AQUA;
        }
        return TextColor.WHITE;
    }

    // https://wiki.vg/Protocol_version_numbers
    private enum MinecraftVersion {
        V1_20_2(764, 1, 20, 2),
        V1_20(763, 1, 20, 0),
        V1_19(759, 1, 19, 0),
        V1_18(757, 1, 18, 0),
        V1_17(755, 1, 17, 0),
        V1_16(735, 1, 16, 0),
        V1_15(573, 1, 15, 0),
        V1_14(477, 1, 14, 0),
        V1_13(393, 1, 13, 0),
        V1_12(335, 1, 12, 0),
        V1_11(315, 1, 11, 0),
        V1_10(210, 1, 10, 0),
        V1_9(107, 1, 9, 0),
        V1_8(47, 1, 8, 0);

        final Version version;
        final int protocolVersion;

        MinecraftVersion(int protocolVersion, int major, int minor, int patch) {
            this.protocolVersion = protocolVersion;
            this.version = new Version(major, minor, patch);
        }
    }
}
