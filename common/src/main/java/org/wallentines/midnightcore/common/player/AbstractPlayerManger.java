package org.wallentines.midnightcore.common.player;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.player.PlayerManager;

import java.util.*;

public abstract class AbstractPlayerManger<T> implements PlayerManager {

    private final HashMap<UUID, AbstractPlayer<T>> cache = new HashMap<>();

    protected abstract AbstractPlayer<T> createPlayer(UUID u);

    protected void cleanupPlayer(UUID u) {

        cache.remove(u);
    }

    protected void cachePlayer(UUID u, T player) {

        cache.compute(u, (k, v) -> {
            if (v == null) v = createPlayer(u);
            v.onLogin(player);
            return v;
        });
    }

    @Override
    public MPlayer getPlayer(UUID u) {

        if(u == null) return null;
        return cache.computeIfAbsent(u, k -> createPlayer(u));
    }

    @Override
    public MPlayer findPlayer(String s) {

        UUID u = toUUID(s);
        return getPlayer(u);
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

    protected abstract UUID toUUID(String name);
}
