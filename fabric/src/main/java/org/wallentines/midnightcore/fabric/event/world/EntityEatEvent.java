package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightlib.event.Event;

public class EntityEatEvent extends Event {

    private final LivingEntity entity;
    private final ItemStack itemStack;

    private boolean cancelled = false;

    public EntityEatEvent(LivingEntity entity, ItemStack itemStack) {
        this.entity = entity;
        this.itemStack = itemStack;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
