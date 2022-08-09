package org.wallentines.midnightcore.fabric.event.entity;

import net.minecraft.world.entity.Entity;
import org.wallentines.midnightlib.event.Event;

public class EntityDismountVehicleEvent extends Event {

    private final Entity vehicle;
    private final Entity rider;

    private boolean cancelled = false;

    public EntityDismountVehicleEvent(Entity vehicle, Entity rider) {
        this.vehicle = vehicle;
        this.rider = rider;
    }

    public Entity getVehicle() {
        return vehicle;
    }

    public Entity getRider() {
        return rider;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
