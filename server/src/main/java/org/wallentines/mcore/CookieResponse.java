package org.wallentines.mcore;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightlib.registry.Identifier;

public class CookieResponse {

    private final Player player;
    private final Identifier id;
    private final ByteBuf data;

    public CookieResponse(Player player, Identifier id, ByteBuf data) {
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

    public ByteBuf data() {
        return data;
    }
}
