package org.wallentines.midnightcore.spigot.adapter;

import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.module.skin.Skin;

public interface SkinUpdater {

    void init();

    void updateSkin(Player user, Skin skin);

}
