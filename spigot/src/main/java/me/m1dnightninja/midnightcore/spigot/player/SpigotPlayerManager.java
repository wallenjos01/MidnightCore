package me.m1dnightninja.midnightcore.spigot.player;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.MPlayerManager;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class SpigotPlayerManager extends MPlayerManager implements Listener {

    public SpigotPlayerManager() {

        Bukkit.getPluginManager().registerEvents(this, MidnightCore.getInstance());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        cleanupPlayer(event.getPlayer().getUniqueId());
    }

    @Override
    protected MPlayer createPlayer(UUID u) {

        return new SpigotPlayer(u);
    }
}
