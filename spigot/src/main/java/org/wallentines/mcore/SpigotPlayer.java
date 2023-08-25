package org.wallentines.mcore;

import org.bukkit.Bukkit;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.wallentines.mcore.item.ItemStack;
import org.wallentines.mcore.item.SpigotItem;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.math.Vec3d;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;

public class SpigotPlayer implements Player {

    private final Server server;
    private final org.bukkit.entity.Player internal;

    public SpigotPlayer(Server server, @NotNull org.bukkit.entity.Player internal) {
        this.server = server;
        this.internal = internal;
    }

    @Override
    public UUID getUUID() {
        return internal.getUniqueId();
    }

    @Override
    public Identifier getType() {
        return new Identifier("minecraft", "player");
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public Component getDisplayName() {
        return Component.text(internal.getDisplayName());
    }

    @Override
    public Identifier getDimensionId() {
        return Identifier.parseOrDefault(internal.getWorld().getName(), "minecraft");
    }

    @Override
    public Vec3d getPosition() {
        org.bukkit.Location location = internal.getLocation();
        return new Vec3d(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public float getYaw() {
        return internal.getLocation().getYaw();
    }

    @Override
    public float getPitch() {
        return internal.getLocation().getPitch();
    }

    @Override
    public boolean isRemoved() {
        return !internal.isOnline();
    }

    @Override
    public void teleport(Location location) {
        internal.teleport(new org.bukkit.Location(
                Bukkit.getWorld(location.dimension.toString()),
                location.position.getX(),
                location.position.getY(),
                location.position.getZ(),
                location.yaw,
                location.pitch
        ));
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {

        SpigotItem si = (SpigotItem) item;

        switch (slot) {
            case FEET: {
                internal.getInventory().setItem(org.bukkit.inventory.EquipmentSlot.FEET, si.getInternal());
                break;
            }
        }
    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public void sendMessage(Component component) {

    }

    @Override
    public void sendActionBar(Component component) {

    }

    @Override
    public void sendTitle(Component title) {

    }

    @Override
    public void sendSubtitle(Component title) {

    }

    @Override
    public void clearTitles() {

    }

    @Override
    public void setTitleTimes(int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void resetTitles() {

    }

    @Override
    public void giveItem(ItemStack item) {

    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public GameMode getGameMode() {
        return null;
    }

    @Override
    public void setGameMode(GameMode mode) {

    }

    @Override
    public boolean isOnline() {
        return internal.isOnline();
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public boolean hasPermission(String permission, int defaultOpLevel) {
        return false;
    }

    @Override
    public void kick(Component message) {

    }

    @Override
    public Skin getSkin() {
        return null;
    }
}
