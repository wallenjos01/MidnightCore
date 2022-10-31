package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.wallentines.midnightlib.event.Event;

import java.util.List;

public class ExplosionEvent extends Event {

    private final Level level;
    private final List<BlockPos> affectedBlocks;
    private final Entity source;

    private boolean cancelled = false;

    public ExplosionEvent(Level level, List<BlockPos> affectedBlocks, Entity source) {
        this.level = level;
        this.affectedBlocks = affectedBlocks;
        this.source = source;
    }

    public Level getLevel() {
        return level;
    }

    public List<BlockPos> getAffectedBlocks() {
        return affectedBlocks;
    }

    public Entity getSource() {
        return source;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
