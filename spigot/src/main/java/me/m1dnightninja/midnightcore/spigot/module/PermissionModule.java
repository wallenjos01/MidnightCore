package me.m1dnightninja.midnightcore.spigot.module;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IPermissionModule;
import me.m1dnightninja.midnightcore.api.permission.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermissionModule implements IPermissionModule {

    @Override
    public boolean initialize(ConfigSection configuration) {
        return true;
    }

    @Override
    public String getId() {
        return "permission";
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return null;
    }

    @Override
    public boolean hasPermission(UUID u, String permission) {
        Player p = Bukkit.getPlayer(u);


        return p != null && p.hasPermission(permission);
    }

    @Override
    public void grantPermission(UUID u, String permission) {

    }

    @Override
    public void clearPermission(UUID u, String permission) {

    }

    @Override
    public Iterable<String> getPermissions(UUID u) {
        return null;
    }

    @Override
    public Iterable<Group> getGroups(UUID u) {
        return null;
    }

    @Override
    public boolean groupHasPermission(Group g, String permission) {
        return false;
    }

    @Override
    public void grantGroupPermission(Group g, String permission) {

    }

    @Override
    public void clearGroupPermission(Group g, String permission) {

    }

    @Override
    public Iterable<String> getGroupPermissions(Group g) {
        return null;
    }

    @Override
    public Group getGroup(String id) {
        return null;
    }

    @Override
    public void reloadAllPermissions() {

    }

    @Override
    public boolean registerGroup(Group g) {
        return false;
    }
}
