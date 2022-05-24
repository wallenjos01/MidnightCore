package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.wallentines.midnightlib.event.Event;

public class PlayerInteractEntityEvent extends Event {

    private final ServerPlayer player;
    private final Entity clicked;
    private final InteractionHand hand;
    private final Vec3 location;

    private boolean cancelled = false;
    private boolean shouldSwingArm = false;

    public PlayerInteractEntityEvent(ServerPlayer player, Entity clicked, InteractionHand hand, Vec3 location) {
        this.player = player;
        this.clicked = clicked;
        this.hand = hand;
        this.location = location;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Entity getClicked() {
        return clicked;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public Vec3 getLocation() {
        return location;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean shouldSwingArm() {
        return shouldSwingArm;
    }

    public void setShouldSwingArm(boolean shouldSwingArm) {
        this.shouldSwingArm = shouldSwingArm;
    }

}
