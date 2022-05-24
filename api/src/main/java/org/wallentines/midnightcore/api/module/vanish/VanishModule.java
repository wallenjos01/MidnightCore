package org.wallentines.midnightcore.api.module.vanish;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.module.Module;

public interface VanishModule extends Module<MidnightCoreAPI> {

    void vanishPlayer(MPlayer player);

    void vanishPlayerFor(MPlayer player, MPlayer observer);

    void revealPlayer(MPlayer player);

    void revealPlayerFor(MPlayer player, MPlayer observer);

    boolean isVanished(MPlayer player);

    boolean isVanishedFor(MPlayer player, MPlayer observer);

}
