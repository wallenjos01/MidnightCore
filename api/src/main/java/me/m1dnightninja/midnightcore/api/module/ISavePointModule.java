package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.IModule;

import java.util.UUID;

public interface ISavePointModule extends IModule {

    void savePlayer(UUID u, String name);
    void loadPlayer(UUID u, String name);
    void removeSavePoint(UUID u, String name);
    void resetPlayer(UUID u);

}
