package org.wallentines.midnightcore.api.player;

import java.util.UUID;

public interface PlayerManager extends Iterable<MPlayer> {

    MPlayer getPlayer(UUID u);

    MPlayer findPlayer(String s);

}
