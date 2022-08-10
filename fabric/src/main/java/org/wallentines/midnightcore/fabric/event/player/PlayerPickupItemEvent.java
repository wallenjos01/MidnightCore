package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import org.wallentines.midnightlib.event.Event;

public class PlayerPickupItemEvent extends Event {

    private final ServerPlayer player;
    private final ItemEntity entity;

    private boolean cancelled = false;

    public PlayerPickupItemEvent(ServerPlayer player, ItemEntity entity) {
        this.player = player;
        this.entity = entity;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ItemEntity getEntity() {
        return entity;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
