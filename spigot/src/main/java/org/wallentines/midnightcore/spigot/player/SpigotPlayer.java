package org.wallentines.midnightcore.spigot.player;

import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.common.player.AbstractPlayer;
import org.wallentines.midnightcore.spigot.adapter.AdapterManager;
import org.wallentines.midnightcore.spigot.item.ItemConverters;
import org.wallentines.midnightcore.spigot.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Consumer;

public class SpigotPlayer extends AbstractPlayer<Player> {

    protected SpigotPlayer(UUID uuid) {
        super(uuid);
    }

    @Override
    public String getUsername() {
        return run(pl -> {
          return AdapterManager.getAdapter().getGameProfile(pl).getName();
        }, () -> getUUID().toString());
    }

    @Override
    public MComponent getName() {
        return run(pl -> {
            return MComponent.parse(pl.getDisplayName());
        }, () -> new MTextComponent(getUUID().toString()));
    }

    @Override
    public Location getLocation() {
        return run(pl -> ConversionUtil.toLocation(pl.getLocation()), () -> null);
    }

    @Override
    public MItemStack getItemInMainHand() {
        return run(pl -> ItemConverters.convertItem(AdapterManager.getAdapter().getItemInMainHand(pl)), () -> null);
    }

    @Override
    public MItemStack getItemInOffhand() {
        return run(pl -> ItemConverters.convertItem(AdapterManager.getAdapter().getItemInOffHand(pl)), () -> null);
    }

    @Override
    public String getLocale() {

        return run(pl -> {
            if(MidnightCoreAPI.getInstance().getGameVersion().getMinorVersion() >= 12) {
                return pl.getLocale();
            }
            return null;
        }, () -> null);
    }

    @Override
    public boolean hasPermission(String permission) {
        return run(pl -> pl.hasPermission(permission), () -> false);
    }

    @Override
    public boolean hasPermission(String permission, int permissionLevel) {
        return run(pl -> pl.hasPermission(permission) || AdapterManager.getAdapter().hasOpLevel(pl, permissionLevel), () -> false);
    }

    @Override
    public void sendMessage(MComponent component) {
        run(pl -> AdapterManager.getAdapter().sendMessage(pl, component), () -> {});
    }

    @Override
    public void sendActionBar(MComponent component) {
        run(pl -> AdapterManager.getAdapter().sendActionBar(pl, component), () -> {});
    }

    @Override
    public void sendTitle(MComponent component, int fadeIn, int stay, int fadeOut) {
        run(pl -> AdapterManager.getAdapter().sendTitle(pl, component, fadeIn, stay, fadeOut), () -> {});
    }

    @Override
    public void sendSubtitle(MComponent component, int fadeIn, int stay, int fadeOut) {
        run(pl -> AdapterManager.getAdapter().sendSubtitle(pl, component, fadeIn, stay, fadeOut), () -> {});
    }

    @Override
    public void clearTitles() {
        run(pl -> AdapterManager.getAdapter().clearTitles(pl), () -> {});
    }

    @Override
    public void playSound(Identifier soundId, String category, float volume, float pitch) {
        run(pl -> pl.playSound(pl.getLocation(), soundId.toString(), SoundCategory.valueOf(category.toUpperCase(Locale.ENGLISH)), volume, pitch), () -> {});
    }

    @Override
    public void closeContainer() {
        run(HumanEntity::closeInventory, () -> {});
    }

    @Override
    public void executeCommand(String cmd) {
        run(pl -> Bukkit.getServer().dispatchCommand(pl, cmd), () -> {});
    }

    @Override
    public void sendChatMessage(String message) {
        run(pl -> pl.chat(message), () -> {});
    }

    @Override
    public void giveItem(MItemStack item) {
        run(pl -> ItemConverters.giveItem(pl, item), () -> {});
    }

    @Override
    public void giveItem(MItemStack item, int slot) {
        run(pl -> ItemConverters.giveItem(pl, item, slot), () -> {});
    }

    @Override
    public void teleport(Location newLoc) {
        run(pl -> pl.teleport(ConversionUtil.toBukkitLocation(newLoc)), () -> {});
    }

    @Override
    public void setGameMode(GameMode gameMode) {

    }

    @Override
    public GameMode getGameMode() {
        return null;
    }

    @Override
    public float getHealth() {
        return run(pl -> (float) pl.getHealth(), () -> 0.0f);
    }

    @Override
    public void applyResourcePack(String url, String hash, boolean force, MComponent promptMessage, Consumer<ResourcePackStatus> onResponse) {

    }


    public static SpigotPlayer wrap(Player player) {
        return (SpigotPlayer) MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
    }

}
