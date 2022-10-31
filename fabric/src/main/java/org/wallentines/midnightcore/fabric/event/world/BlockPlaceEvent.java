package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import org.wallentines.midnightlib.event.Event;

public class BlockPlaceEvent extends Event {

    private final ServerPlayer player;
    private final BlockPos pos;
    private final BlockItem item;
    private final InteractionHand hand;

    private BlockState state;
    private boolean cancelled = false;

    public BlockPlaceEvent(ServerPlayer player, BlockPos pos, BlockItem item, BlockState placementState, InteractionHand hand) {
        this.player = player;
        this.pos = pos;
        this.item = item;
        this.hand = hand;
        this.state = placementState;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockItem getItem() {
        return item;
    }

    public InteractionHand getHand() {
        return hand;
    }

    public BlockState getPlacedState() {
        return state;
    }

    public void setPlacedState(BlockState state) {
        this.state = state;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
