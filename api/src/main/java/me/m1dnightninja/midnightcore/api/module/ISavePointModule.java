package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.player.MPlayer;

public interface ISavePointModule extends IModule {

    void savePlayer(MPlayer pl, String id);

    void loadPlayer(MPlayer pl, String id);

    void removeSavePoint(MPlayer pl, String id);

    void resetPlayer(MPlayer pl);
}

