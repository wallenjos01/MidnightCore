package org.wallentines.midnightcore.spigot.module.vanish;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.module.vanish.AbstractVanishModule;
import org.wallentines.midnightcore.spigot.MidnightCore;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;

public class SpigotVanishModule extends AbstractVanishModule implements Listener {

    @Override
    protected void doVanish(MPlayer player, MPlayer observer) {

        ((SpigotPlayer) observer).getInternal().hidePlayer(MidnightCore.getInstance(), ((SpigotPlayer) player).getInternal());

    }

    @Override
    protected void doReveal(MPlayer player, MPlayer observer) {

        ((SpigotPlayer) observer).getInternal().showPlayer(MidnightCore.getInstance(), ((SpigotPlayer) player).getInternal());
    }

    @Override
    protected void registerEvents() {

        Bukkit.getPluginManager().registerEvents(this, MidnightCore.getInstance());

    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {

        SpigotPlayer sp = SpigotPlayer.wrap(event.getPlayer());
        if(isVanished(sp)) {
            if (hideMessages) event.setJoinMessage(null);
        }
        onJoin(sp);
    }

    @EventHandler
    private void onLeave(PlayerQuitEvent event) {

        if (hideMessages) event.setQuitMessage(null);
        if(isVanished(SpigotPlayer.wrap(event.getPlayer()))) {
            if (hideMessages) event.setQuitMessage(null);
        }
    }
}
