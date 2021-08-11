package me.m1dnightninja.midnightcore.api.player;

import java.util.*;

public abstract class MPlayerManager implements Iterable<MPlayer> {

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

    public void cleanupPlayer(UUID u) {

        if(cache.containsKey(u)) {
            cache.get(u).cleanup();
        }
    }

    @Override
    public Iterator<MPlayer> iterator() {
        return new Iterator<MPlayer>() {

            private int current = 0;
            private final List<MPlayer> players = new ArrayList<>(cache.values());

            @Override
            public boolean hasNext() {
                return current < players.size();
            }

            @Override
            public MPlayer next() {
                MPlayer out = players.get(current);
                current++;

                return out;
            }
        };
    }
}
