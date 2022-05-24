package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.wallentines.midnightlib.event.Event;

import java.util.List;

public class ExplosionEvent extends Event {

    private final List<BlockPos> affectedBlocks;
    private final Entity source;

    private boolean cancelled = false;

    public ExplosionEvent(List<BlockPos> affectedBlocks, Entity source) {
        this.affectedBlocks = affectedBlocks;
        this.source = source;
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
