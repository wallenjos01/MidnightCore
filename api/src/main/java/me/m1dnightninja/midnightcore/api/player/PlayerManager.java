package me.m1dnightninja.midnightcore.api.player;

import java.util.HashMap;
import java.util.UUID;

public abstract class PlayerManager {

    private final HashMap<UUID, MPlayer> cache = new HashMap<>();

    public void cachePlayer(MPlayer pl) {

        if(pl == null) return;

        cache.put(pl.getUUID(), pl);
    }

    public MPlayer getPlayer(UUID u) {

        if(u == null) return null;

        if(!cache.containsKey(u)) {

            cachePlayer(createPlayer(u));
        }
        return cache.get(u);
    }

    protected abstract MPlayer createPlayer(UUID u);

    protected void uncachePlayer(UUID u) {
        cache.remove(u);
    }

}
