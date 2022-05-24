package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.wallentines.midnightlib.event.Event;

public class PlayerAttackEntityEvent extends Event {

    private final ServerPlayer player;
    private final Entity entity;

    private boolean cancelled;

    public PlayerAttackEntityEvent(ServerPlayer player, Entity entity) {
        this.player = player;
        this.entity = entity;
    }

    public ServerPlayer getPlayer() {
        return player;
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
