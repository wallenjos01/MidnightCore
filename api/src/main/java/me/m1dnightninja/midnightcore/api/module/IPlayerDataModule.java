package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.util.UUID;

public interface IPlayerDataModule extends IModule {

    ConfigSection getPlayerData(UUID u);

    void setPlayerData(UUID u, ConfigSection sec);

    void savePlayerData(UUID u);

    void clearPlayerData(UUID u);

    void reloadPlayerData(UUID u);

}
