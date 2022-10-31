package org.wallentines.midnightcore.fabric.event.entity;

import net.minecraft.world.entity.Entity;
import org.wallentines.midnightlib.event.Event;

public class EntityLoadedEvent extends Event {

    private final Entity entity;

    public EntityLoadedEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
