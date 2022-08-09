package org.wallentines.midnightcore.fabric.event.entity;

import net.minecraft.world.entity.Entity;
import org.wallentines.midnightlib.event.Event;

public class EntitySpawnEvent extends Event {

    private final Entity entity;
    private boolean cancelled = false;

    public EntitySpawnEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
