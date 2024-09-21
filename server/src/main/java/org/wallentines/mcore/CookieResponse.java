package org.wallentines.mcore;

import org.wallentines.midnightlib.registry.Identifier;

public class CookieResponse {

    private final Player player;
    private final Identifier id;
    private final byte[] data;

    public CookieResponse(Player player, Identifier id, byte[] data) {
        this.player = player;
        this.id = id;
        this.data = data;
    }

    public Player player() {
        return player;
    }

    public Identifier id() {
        return id;
    }

    public byte[] data() {
        return data;
    }
}
