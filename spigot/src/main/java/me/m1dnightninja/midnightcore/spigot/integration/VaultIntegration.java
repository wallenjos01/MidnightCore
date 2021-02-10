package me.m1dnightninja.midnightcore.spigot.integration;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultIntegration {

    Permission perms;

    public VaultIntegration() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if(rsp == null) return;

        perms = rsp.getProvider();
    }

    public void grantPermission(Player p, String permission) {

        perms.playerAdd(p, permission);
    }

    public void clearPermission(Player p, String permission) {

        perms.playerRemove(p, permission);
    }

}
