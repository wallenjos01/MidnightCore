package me.m1dnightninja.midnightcore.api.module.vanish;

import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

public interface IVanishModule extends IModule {

    void hidePlayer(MPlayer player);

    void hidePlayerFor(MPlayer player, MPlayer other);

    void showPlayer(MPlayer player);

    void showPlayerFor(MPlayer player, MPlayer other);

}
