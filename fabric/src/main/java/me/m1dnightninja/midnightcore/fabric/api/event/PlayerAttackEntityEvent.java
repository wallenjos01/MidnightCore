package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.world.entity.Entity;

public class PlayerAttackEntityEvent extends Event {

    private final MPlayer player;
    private final Entity entity;

    private boolean cancelled;

    public PlayerAttackEntityEvent(MPlayer player, Entity entity) {
        this.player = player;
        this.entity = entity;
    }

    public MPlayer getPlayer() {
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
