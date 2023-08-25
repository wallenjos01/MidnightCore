package org.wallentines.mcore.adapter;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.Skin;

public interface SkinUpdater {

    /**
     * Changes the active skin for the given player, then sends updates to online players
     * @param player The player to update
     * @param skin The skin to apply
     */
    void changePlayerSkin(Player player, @Nullable Skin skin);

}
