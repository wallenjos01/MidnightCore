package me.m1dnightninja.midnightcore.fabric.player;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.PlayerManager;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;

import java.util.UUID;

public class FabricPlayerManager extends PlayerManager {

    @Override
    protected MPlayer createPlayer(UUID u) {

        return new FabricPlayer(u);
    }

}
