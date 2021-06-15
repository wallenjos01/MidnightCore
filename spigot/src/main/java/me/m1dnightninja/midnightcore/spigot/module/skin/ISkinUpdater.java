package me.m1dnightninja.midnightcore.spigot.module.skin;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface ISkinUpdater {

    boolean initialize();

    void updatePlayer(Player player, Skin skin, Collection<? extends Player> observers);
}

