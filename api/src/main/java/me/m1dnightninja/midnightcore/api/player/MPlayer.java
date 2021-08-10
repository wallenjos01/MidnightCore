package me.m1dnightninja.midnightcore.api.player;

import me.m1dnightninja.midnightcore.api.inventory.MItemStack;
import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.UUID;

public abstract class MPlayer {

    private final UUID u;

    protected MPlayer(UUID u) {
        if(u == null) throw new IllegalStateException("UUID cannot be null!");
        this.u = u;
    }

    public UUID getUUID() {
        return u;
    }

    public abstract MComponent getName();
    public abstract MComponent getDisplayName();
    public abstract MIdentifier getDimension();
    public abstract Skin getSkin();
    public abstract Vec3d getLocation();

    public abstract MItemStack getItemInMainHand();
    public abstract MItemStack getItemInOffHand();

    public abstract float getYaw();
    public abstract float getPitch();

    public abstract boolean isOffline();

    public abstract boolean hasPermission(String perm);

    public abstract void sendMessage(MComponent comp);
    public abstract void sendTitle(MTitle title);
    public abstract void sendActionBar(MActionBar ab);

    public abstract void executeCommand(String cmd);
    public abstract void sendChatMessage(String message);

    public abstract void teleport(MIdentifier dimension, Vec3d location, float yaw, float pitch);
    public abstract void teleport(Vec3d location, float yaw, float pitch);

    public abstract void giveItem(MItemStack item);

    protected abstract void cleanup();

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof MPlayer) {
            return ((MPlayer) obj).getUUID().equals(u);
        }
        return false;
    }
}
