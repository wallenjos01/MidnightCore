package org.wallentines.midnightcore.velocity.player;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.item.MItemStack;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.api.text.MTextComponent;
import org.wallentines.midnightcore.common.player.AbstractPlayer;
import org.wallentines.midnightcore.velocity.MidnightCore;
import org.wallentines.midnightcore.velocity.util.ConversionUtil;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class VelocityPlayer extends AbstractPlayer<Player> {

    protected VelocityPlayer(UUID uuid) {
        super(uuid);
    }

    @Override
    public String getUsername() {
        return run(Player::getUsername, () -> getUUID().toString());
    }

    @Override
    public MComponent getName() {
        return run(pl -> {
            return new MTextComponent(pl.getUsername());
        }, () -> new MTextComponent(getUUID().toString()));
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public MItemStack getItemInMainHand() {
        return null;
    }

    @Override
    public MItemStack getItemInOffhand() {
        return null;
    }

    @Override
    public String getLocale() {
        return run(pl -> Objects.requireNonNull(pl.getEffectiveLocale()).toString(), () -> "");
    }

    @Override
    public boolean hasPermission(String permission) {
        return run(pl -> pl.hasPermission(permission), () -> false);
    }

    @Override
    public boolean hasPermission(String permission, int permissionLevel) {
        return hasPermission(permission);
    }

    @Override
    public void sendMessage(MComponent component) {
        run(pl -> pl.sendMessage(ConversionUtil.toComponent(component)), () -> {});
    }

    @Override
    public void sendActionBar(MComponent component) {

        run(pl -> pl.sendActionBar(ConversionUtil.toComponent(component)), () -> {});
    }

    @Override
    public void sendTitle(MComponent component, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void sendSubtitle(MComponent component, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void clearTitles() {

        run(Audience::clearTitle, () -> {});
    }

    @Override
    public void playSound(Identifier soundId, String category, float volume, float pitch) {

        run(pl -> {
            Key key = ConversionUtil.toKey(soundId);
            Sound.Source cat = Sound.Source.valueOf(category);

            pl.playSound(Sound.sound(key, cat, volume, pitch));
        }, () -> {});

    }

    @Override
    public void closeContainer() {

    }

    @Override
    public void executeCommand(String cmd) {
        run(pl -> MidnightCore.getInstance().getServer().getCommandManager().executeAsync(pl, cmd), () -> {});
    }

    @Override
    public void sendChatMessage(String message) {
        run(pl -> pl.spoofChatInput(message), () -> {});
    }

    @Override
    public void giveItem(MItemStack item) {

    }

    @Override
    public void giveItem(MItemStack item, int slot) {

    }

    @Override
    public void teleport(Location newLoc) {

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
        return 0;
    }

    @Override
    public void applyResourcePack(String url, String hash, boolean force, MComponent promptMessage, Consumer<ResourcePackStatus> onResponse) {

    }

    public static MPlayer wrap(Player player) {
        return MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
    }
}
