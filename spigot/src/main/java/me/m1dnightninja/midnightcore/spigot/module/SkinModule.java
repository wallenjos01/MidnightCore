package me.m1dnightninja.midnightcore.spigot.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.common.MojangUtil;
import me.m1dnightninja.midnightcore.common.module.AbstractSkinModule;
import me.m1dnightninja.midnightcore.spigot.skin.ISkinUpdater;
import me.m1dnightninja.midnightcore.spigot.skin.SkinUpdater_16;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.UUID;

public class SkinModule extends AbstractSkinModule implements Listener {

    ISkinUpdater updater;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if(updater == null) return;
        loginSkins.put(event.getPlayer().getUniqueId(), MojangUtil.getSkinFromProfile(updater.getProfile(event.getPlayer())));
        activeSkins.put(event.getPlayer().getUniqueId(), loginSkins.get(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(updater == null) return;
        for(UUID u : activeSkins.keySet()) {

            if(activeSkins.get(u) == loginSkins.get(u)) continue;

            Player p = Bukkit.getPlayer(u);
            updater.updatePlayer(p, activeSkins.get(u), Collections.singletonList(event.getPlayer()));

        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        loginSkins.remove(event.getPlayer().getUniqueId());
        loadedSkins.remove(event.getPlayer().getUniqueId());
        activeSkins.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public boolean initialize(ConfigSection section) {

        switch(ReflectionUtil.API_VERSION) {
            case "v1_16_R1":
            case "v1_16_R2":
            case "v1_16_R3":
                updater = new SkinUpdater_16();
                break;

        }

        if(updater == null || !updater.initialize()) {
            MidnightCoreAPI.getLogger().warn("Can't enable skin module! Version unsupported!");
            return false;
        }

        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {
        return null;
    }

    @Override
    public void updateSkin(UUID uid) {

        if(updater == null) return;

        Player p = Bukkit.getPlayer(uid);
        if(p == null) return;

        activeSkins.put(uid, loadedSkins.get(uid));
        loadedSkins.remove(uid);

        updater.updatePlayer(p, activeSkins.get(uid), Bukkit.getOnlinePlayers());

    }

}
