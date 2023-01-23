package org.wallentines.midnightcore.api.module.vanish;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;

@SuppressWarnings("unused")
public interface VanishModule extends ServerModule {

    void vanishPlayer(MPlayer player);

    void vanishPlayerFor(MPlayer player, MPlayer observer);

    void revealPlayer(MPlayer player);

    void revealPlayerFor(MPlayer player, MPlayer observer);

    boolean isVanished(MPlayer player);

    boolean isVanishedFor(MPlayer player, MPlayer observer);

}
