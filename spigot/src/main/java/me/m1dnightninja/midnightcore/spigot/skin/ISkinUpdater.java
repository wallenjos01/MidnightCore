package me.m1dnightninja.midnightcore.spigot.skin;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface ISkinUpdater {

    boolean initialize();

    GameProfile getProfile(Player player);

    void updatePlayer(Player player, Skin skin, Collection<? extends Player> observers);
}

