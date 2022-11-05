package org.wallentines.midnightcore.spigot.adapter;

import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.module.skin.Skin;

public interface SkinUpdater {

    boolean init();

    void updateSkin(Player user, Skin skin);

}
