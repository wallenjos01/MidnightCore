package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public class ChunkLoadEvent extends Event {

    private final ChunkPos position;
    private final ServerLevel level;

    public ChunkLoadEvent(ChunkPos position, ServerLevel level) {
        this.position = position;
        this.level = level;
    }

    public ChunkPos getPosition() {
        return position;
    }

    public ServerLevel getLevel() {
        return level;
    }
}
