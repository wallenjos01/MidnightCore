package org.wallentines.mcore.adapter.v1_20_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.SharedConstants;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.GameVersion;
import org.wallentines.mcore.Skin;
import org.wallentines.mcore.adapter.Adapter;
import org.wallentines.mcore.adapter.SkinUpdater;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.ConfigSection;

public class AdapterImpl implements Adapter {

    private SkinUpdaterImpl updater;

    @Override
    public boolean initialize() {

        updater = new SkinUpdaterImpl();
        return true;
    }

    @Override
    public void runOnServer(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().b().a(runnable);
    }

    @Override
    public void addTickListener(Runnable runnable) {
        CraftServer server = (CraftServer) Bukkit.getServer();
        server.getHandle().b().b(runnable);
    }

    @Override
    public @Nullable Skin getPlayerSkin(Player player) {

        CraftPlayer craftPlayer = (CraftPlayer) player;
        GameProfile profile = craftPlayer.getProfile();

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

    }

    @Override
    public void sendActionBar(Player player, Component component) {

    }

    @Override
    public void sendTitle(Player player, Component component) {

    }

    @Override
    public void sendSubtitle(Player player, Component component) {

    }

    @Override
    public void setTitleAnimation(Player player, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void clearTitles(Player player) {

    }

    @Override
    public void resetTitles(Player player) {

    }

    @Override
    public boolean hasOpLevel(Player player, int level) {
        return false;
    }

    @Override
    public void loadTag(Player player, ConfigSection tag) {

    }

    @Override
    public void setTag(ItemStack itemStack, ConfigSection tag) {

    }

    @Override
    public ConfigSection getTag(ItemStack itemStack) {
        return null;
    }

    @Override
    public ItemStack setupInternal(ItemStack item) {
        return null;
    }

    @Override
    public GameVersion getGameVersion() {
        return new GameVersion(SharedConstants.b().c(), SharedConstants.c()); // getCurrentVersion, getId, getProtocolVersion
    }
}
