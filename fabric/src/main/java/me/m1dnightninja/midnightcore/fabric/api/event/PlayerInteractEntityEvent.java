package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;

public class PlayerInteractEntityEvent extends Event {

    private final ServerPlayer player;
    private final Entity clicked;
    private final InteractionHand hand;

    private boolean cancelled = false;
    private boolean shouldSwingArm = false;

    public PlayerInteractEntityEvent(ServerPlayer player, Entity clicked, InteractionHand hand) {
        this.player = player;
        this.clicked = clicked;
        this.hand = hand;
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
