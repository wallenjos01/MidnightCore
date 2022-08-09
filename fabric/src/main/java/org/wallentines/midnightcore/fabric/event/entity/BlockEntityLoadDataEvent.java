package org.wallentines.midnightcore.fabric.event.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.wallentines.midnightlib.event.Event;

public class BlockEntityLoadDataEvent extends Event {

    private final BlockEntity entity;
    private CompoundTag tag;

    public BlockEntityLoadDataEvent(BlockEntity entity, CompoundTag tag) {
        this.entity = entity;
        this.tag = tag;
    }

    public BlockEntity getEntity() {
        return entity;
    }

    public CompoundTag getTag() {
        return tag;
    }

    public void setTag(CompoundTag tag) {
        this.tag = tag;
    }
}
