package me.m1dnightninja.midnightcore.velocity.player;

import com.velocitypowered.api.proxy.Player;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;

import java.util.UUID;

public class VelocityPlayer extends MPlayer {

    private Player player;

    public VelocityPlayer(UUID u) {
        super(u);
        MidnightCore.getInstance().getServer().getPlayer(u).ifPresent(value -> player = value);
    }

    public static MPlayer wrap(Player player) {
        return MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
    }

    @Override
    public MComponent getName() {

        return MComponent.createTextComponent(player.getUsername());
    }

    @Override
    public MComponent getDisplayName() {

        return getName();
    }

    @Override
    public MIdentifier getDimension() {

        return null;
    }

    @Override
    public Skin getSkin() {

        return null;
    }

    @Override
    public Vec3d getLocation() {

        return null;
    }

    @Override
    public MItemStack getItemInMainHand() {

        return null;
    }

    @Override
    public MItemStack getItemInOffHand() {

        return null;
    }

    @Override
    public float getYaw() {

        return 0;
    }

    @Override
    public float getPitch() {

        return 0;
    }

    @Override
    public boolean isOffline() {

        return player == null || !player.isActive();
    }

    @Override
    public boolean hasPermission(String perm) {

        return player.hasPermission(perm);
    }

    @Override
    public void sendMessage(MComponent comp) {

    }

    @Override
    public void sendTitle(MTitle title) {

    }

    @Override
    public void sendActionBar(MActionBar ab) {

    }

    @Override
    public void executeCommand(String cmd) {

    }

    @Override
    public void sendChatMessage(String message) {

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

    public Player getVelocityPlayer() {
        return player;
    }
}
