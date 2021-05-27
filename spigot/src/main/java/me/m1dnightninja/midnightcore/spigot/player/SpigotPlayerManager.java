package me.m1dnightninja.midnightcore.spigot.player;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpigotPlayerManager extends PlayerManager {

    @Override
    protected MPlayer createPlayer(UUID u) {

        return new SpigotPlayer(u);
    }
}
