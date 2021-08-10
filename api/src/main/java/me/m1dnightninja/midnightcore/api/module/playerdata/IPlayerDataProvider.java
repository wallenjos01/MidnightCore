package me.m1dnightninja.midnightcore.api.module.playerdata;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

import java.util.UUID;

public interface IPlayerDataProvider {

    ConfigSection getPlayerData(UUID u);

    default ConfigSection getPlayerData(MPlayer player) { return getPlayerData(player.getUUID()); }

    void setPlayerData(UUID u, ConfigSection sec);

    default void setPlayerData(MPlayer player, ConfigSection sec) { setPlayerData(player.getUUID(), sec); }

    void savePlayerData(UUID u);

    default void savePlayerData(MPlayer player) { savePlayerData(player.getUUID()); }

    void clearPlayerData(UUID u);

    default void clearPlayerData(MPlayer player) { clearPlayerData(player.getUUID()); }

    void reloadPlayerData(UUID u);

    default void reloadPlayerData(MPlayer player) { reloadPlayerData(player.getUUID()); }

    void saveAllPlayerData();

}
