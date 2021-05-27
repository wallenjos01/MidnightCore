package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

public class PlayerInteractEvent extends Event {

    private final ServerPlayer player;
    private final ItemStack item;
    private final InteractionHand hand;
    private final ServerboundInteractPacket.Action action;
    private final BlockHitResult result;

    private boolean cancelled = false;
    private boolean shouldSwingArm = false;

    public PlayerInteractEvent(ServerPlayer player, ItemStack item, InteractionHand hand, ServerboundInteractPacket.Action action, BlockHitResult result) {
        this.player = player;
        this.item = item;
        this.hand = hand;
        this.action = action;
        this.result = result;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public BlockHitResult getBlockHit() {
        return result;
    }

    public boolean isLeftClick() {
        return action == ServerboundInteractPacket.Action.ATTACK;
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
