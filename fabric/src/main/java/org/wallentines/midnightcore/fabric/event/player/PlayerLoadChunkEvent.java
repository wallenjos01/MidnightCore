package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.wallentines.midnightlib.event.Event;

public class PlayerLoadChunkEvent extends Event {

    private final LevelChunk chunk;
    private final ServerPlayer player;

    public PlayerLoadChunkEvent(LevelChunk chunk, ServerPlayer player) {
        this.chunk = chunk;
        this.player = player;
    }

    public LevelChunk getChunk() {
        return chunk;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
}
