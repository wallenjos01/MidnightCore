package me.m1dnightninja.midnightcore.spigot.module.skin;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;
import me.m1dnightninja.midnightcore.common.module.AbstractSkinModule;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import me.m1dnightninja.midnightcore.spigot.util.NMSWrapper;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import me.m1dnightninja.midnightcore.spigot.version.v1_10.SkinUpdater_10_12;
import me.m1dnightninja.midnightcore.spigot.version.v1_13.SkinUpdater_13;
import me.m1dnightninja.midnightcore.spigot.version.v1_14.SkinUpdater_14;
import me.m1dnightninja.midnightcore.spigot.version.v1_15.SkinUpdater_15;
import me.m1dnightninja.midnightcore.spigot.version.v1_16.SkinUpdater_16_R1;
import me.m1dnightninja.midnightcore.spigot.version.v1_16.SkinUpdater_16_R3;
import me.m1dnightninja.midnightcore.spigot.version.v1_17.SkinUpdater_17;
import me.m1dnightninja.midnightcore.spigot.version.v1_8.SkinUpdater_8;
import me.m1dnightninja.midnightcore.spigot.version.v1_9.SkinUpdater_9;
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

        GameProfile prof = NMSWrapper.getGameProfile(event.getPlayer());
        Skin s = MojangUtil.getSkinFromProfile(prof);
        loginSkins.put(pl, s);

        if(getOfflineModeSkins && !Bukkit.getServer().getOnlineMode()) {

            findOfflineSkin(pl, prof);

        } else {

            activeSkins.put(pl, loginSkins.get(pl));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(updater == null) return;
        for(MPlayer u : activeSkins.keySet()) {

            if(!getOfflineModeSkins && activeSkins.get(u) == loginSkins.get(u)) continue;

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

        super.initialize(section);

        switch(ReflectionUtil.API_VERSION) {
            case "v1_8_R1":
            case "v1_8_R2":
            case "v1_8_R3":
                updater = new SkinUpdater_8();
                break;
            case "v1_9_R1":
            case "v1_9_R2":
            case "v1_9_R3":
                updater = new SkinUpdater_9();
                break;
            case "v1_10_R1":
            case "v1_10_R2":
            case "v1_11_R1":
            case "v1_11_R2":
            case "v1_12_R1":
            case "v1_12_R2":
                updater = new SkinUpdater_10_12();
                break;
            case "v1_13_R1":
            case "v1_13_R2":
            case "v1_13_R3":
                updater = new SkinUpdater_13();
                break;
            case "v1_14_R1":
            case "v1_14_R2":
            case "v1_14_R3":
                updater = new SkinUpdater_14();
                break;
            case "v1_15_R1":
            case "v1_15_R2":
                updater = new SkinUpdater_15();
                break;
            case "v1_16_R1":
                updater = new SkinUpdater_16_R1();
            case "v1_16_R2":
            case "v1_16_R3":
                updater = new SkinUpdater_16_R3();
                break;
            default:
                updater = new SkinUpdater_17();

        }

        Bukkit.getPluginManager().registerEvents(this, MidnightCore.getInstance());

        if(!updater.initialize()) {
            MidnightCoreAPI.getLogger().warn("Can't enable skin module! Version unsupported!");
            return false;
        }

        return true;
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
