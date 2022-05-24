package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.wallentines.midnightlib.event.Event;

public class EntityDamageEvent extends Event {

    private final LivingEntity entity;
    private final DamageSource source;
    private final float amount;

    private boolean cancelled = false;

    public EntityDamageEvent(LivingEntity entity, DamageSource source, float amount) {
        this.entity = entity;
        this.source = source;
        this.amount = amount;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
