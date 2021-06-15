package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.api.math.Vec3d;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerAttackEntityEvent;
import me.m1dnightninja.midnightcore.fabric.api.event.PlayerInteractEntityEvent;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class WrappedHandler implements ServerboundInteractPacket.Handler {

    private final ServerPlayer player;
    private final Entity entity;
    private final ServerboundInteractPacket.Handler other;

    public WrappedHandler(ServerPlayer player, Entity entity, ServerboundInteractPacket.Handler other) {
        this.player = player;
        this.entity = entity;
        this.other = other;
    }

    @Override
    public void onInteraction(InteractionHand interactionHand) {
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
    public void onInteraction(InteractionHand interactionHand, Vec3 vec3) {
        PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(player, entity, interactionHand, new Vec3d(vec3.x, vec3.y, vec3.z));
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

        PlayerAttackEntityEvent event = new PlayerAttackEntityEvent(FabricPlayer.wrap(player), entity);
        Event.invoke(event);

        if (!event.isCancelled()) {
            other.onAttack();
        }

    }
}
