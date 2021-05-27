package me.m1dnightninja.midnightcore.spigot.module.skin;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.midnightcore.common.module.AbstractSkinModule;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;

public class SkinModule extends AbstractSkinModule implements Listener {

    ISkinUpdater updater;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if(updater == null) return;

        MPlayer pl = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());

        Skin s = MojangUtil.getSkinFromProfile(updater.getProfile(event.getPlayer()));
        loginSkins.put(pl, s);
        activeSkins.put(pl, loginSkins.get(pl));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(updater == null) return;
        for(MPlayer u : activeSkins.keySet()) {

            if(activeSkins.get(u) == loginSkins.get(u)) continue;

            Player p = ((SpigotPlayer) u).getSpigotPlayer();
            updater.updatePlayer(p, activeSkins.get(u), Collections.singletonList(event.getPlayer()));

        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        MPlayer pl = MidnightCoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());

        loginSkins.remove(pl);
        loadedSkins.remove(pl);
        activeSkins.remove(pl);
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
    public void updateSkin(MPlayer pl) {

        if(updater == null) return;

        Player p = ((SpigotPlayer) pl).getSpigotPlayer();
        if(p == null) return;

        activeSkins.put(pl, loadedSkins.get(pl));
        loadedSkins.remove(pl);

        updater.updatePlayer(p, activeSkins.get(pl), Bukkit.getOnlinePlayers());

    }

}
