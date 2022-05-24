package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.wallentines.midnightlib.event.Event;

public record WrappedHandler(ServerPlayer player, Entity entity, ServerboundInteractPacket.Handler other) implements ServerboundInteractPacket.Handler {

    @Override
    public void onInteraction(@NotNull InteractionHand interactionHand) {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(player, entity, interactionHand, null);
        Event.invoke(event);

        if (event.isCancelled()) {
            if (event.shouldSwingArm()) {
                player.swing(interactionHand);
            }
        } else {
            other.onInteraction(interactionHand);
        }
    }

    @Override
    public void onInteraction(@NotNull InteractionHand interactionHand, @NotNull Vec3 vec3) {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(player, entity, interactionHand, vec3);
        Event.invoke(event);

        if (event.isCancelled()) {
            if (event.shouldSwingArm()) {
                player.swing(interactionHand);
            }
        } else {
            other.onInteraction(interactionHand, vec3);
        }
    }

    @Override
    public void onAttack() {

        PlayerAttackEntityEvent event = new PlayerAttackEntityEvent(player, entity);
        Event.invoke(event);

        if (!event.isCancelled()) {
            other.onAttack();
        }

    }

}
