package org.wallentines.midnightcore.fabric.event.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.wallentines.midnightlib.event.Event;

public class EntityLoadDataEvent extends Event {

    private final Entity entity;
    private CompoundTag tag;

    public EntityLoadDataEvent(Entity entity, CompoundTag tag) {
        this.entity = entity;
        this.tag = tag;
    }

    public Entity getEntity() {
        return entity;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public void setTag(CompoundTag tag) {
        this.tag = tag;
    }
}
