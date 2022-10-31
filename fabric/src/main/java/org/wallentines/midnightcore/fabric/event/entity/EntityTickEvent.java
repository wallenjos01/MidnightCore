package org.wallentines.midnightcore.fabric.event.entity;

import net.minecraft.world.entity.Entity;
import org.wallentines.midnightlib.event.Event;

public class EntityTickEvent extends Event {

    private final Entity entity;

    public EntityTickEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
