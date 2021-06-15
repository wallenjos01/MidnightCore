package me.m1dnightninja.midnightcore.spigot.player;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.Title;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpigotPlayer extends MPlayer {

    private Player player;

    protected SpigotPlayer(UUID u) {
        super(u);

        updatePlayer();
    }

    private void updatePlayer() {
        if(player == null) player = Bukkit.getPlayer(getUUID());
    }

    @Override
    public MComponent getName() {
        updatePlayer();
        return MComponent.createTextComponent(player.getName());
    }

    @Override
    public MComponent getDisplayName() {
        updatePlayer();
        return MComponent.createTextComponent(player.getDisplayName());
    }

    @Override
    public MIdentifier getDimension() {
        updatePlayer();
        return MIdentifier.parseOrDefault(player.getWorld().getName());
    }

    @Override
    public Skin getSkin() {
        updatePlayer();

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        if(mod != null) {
            return mod.getSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId()));
        }

        return MojangUtil.getSkinFromProfile(NMSWrapper.getGameProfile(player));
    }

    @Override
    public Vec3d getLocation() {
        updatePlayer();
        return new Vec3d(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    @Override
    public float getYaw() {
        updatePlayer();
        return player.getEyeLocation().getYaw();
    }

    @Override
    public float getPitch() {
        updatePlayer();
        return player.getEyeLocation().getPitch();
    }

    @Override
    public boolean isOffline() {
        updatePlayer();
        return player == null;
    }

    @Override
    public void sendMessage(MComponent comp) {
        updatePlayer();

        NMSWrapper.sendMessage(player, comp);
    }

    @Override
    public void sendTitle(Title title) {

    }

    @Override
    public void sendActionBar(ActionBar ab) {

    }

    @Override
    public void teleport(MIdentifier dimension, Vec3d location, float yaw, float pitch) {

    }

    @Override
    public void teleport(Vec3d location, float yaw, float pitch) {

    }

    @Override
    public void giveItem(MItemStack item) {

    }

    @Override
    protected void cleanup() {
        player = null;
    }

    @Override
    public boolean hasPermission(String perm) {

        updatePlayer();
        return player.hasPermission(perm);
    }

    @Nullable
    public Player getSpigotPlayer() {
        updatePlayer();
        return player;
    }

    public static SpigotPlayer wrap(Player player) {
        return (SpigotPlayer) MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
    }
}
