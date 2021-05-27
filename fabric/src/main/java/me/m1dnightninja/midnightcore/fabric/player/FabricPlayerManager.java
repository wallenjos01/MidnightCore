package me.m1dnightninja.midnightcore.fabric.player;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.player.PlayerManager;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerDisconnectEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;

import java.util.UUID;

public class FabricPlayerManager extends PlayerManager {

    public FabricPlayerManager() {

        Event.register(PlayerDisconnectEvent.class, this, 100, event -> {
            uncachePlayer(event.getPlayer().getUUID());
        });

    }

    @Override
    protected MPlayer createPlayer(UUID u) {

        return new FabricPlayer(u);
    }

}
