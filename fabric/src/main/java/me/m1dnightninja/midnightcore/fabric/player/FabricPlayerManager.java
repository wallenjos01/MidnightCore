package me.m1dnightninja.midnightcore.fabric.player;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.MPlayerManager;

import java.util.UUID;

public class FabricPlayerManager extends MPlayerManager {

    @Override
    protected MPlayer createPlayer(UUID u) {

        return new FabricPlayer(u);
    }

}
