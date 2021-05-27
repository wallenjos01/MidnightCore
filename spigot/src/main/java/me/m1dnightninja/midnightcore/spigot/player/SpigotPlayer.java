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
import me.m1dnightninja.midnightcore.spigot.util.NMSUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class SpigotPlayer extends MPlayer {

    private Player player;

    protected SpigotPlayer(UUID u) {
        super(u);

        this.player = Bukkit.getPlayer(u);
    }

    @Override
    public MComponent getName() {
        return MComponent.createTextComponent(player.getName());
    }

    @Override
    public MComponent getDisplayName() {
        return MComponent.createTextComponent(player.getDisplayName());
    }

    @Override
    public MIdentifier getDimension() {
        return MIdentifier.parseOrDefault(player.getWorld().getName());
    }

    @Override
    public Skin getSkin() {

        ISkinModule mod = MidnightCoreAPI.getInstance().getModule(ISkinModule.class);
        if(mod != null) {
            return mod.getSkin(MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId()));
        }

        return MojangUtil.getSkinFromProfile(NMSUtil.getGameProfile(player));
    }

    @Override
    public Vec3d getLocation() {
        return new Vec3d(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    @Override
    public void sendMessage(MComponent comp) {
        NMSUtil.sendMessage(player, comp);
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
    public boolean hasPermission(String perm) {

        return player.hasPermission(perm);
    }

    @Nullable
    public Player getSpigotPlayer() {
        if(player == null) player = Bukkit.getPlayer(getUUID());
        return player;
    }
}
