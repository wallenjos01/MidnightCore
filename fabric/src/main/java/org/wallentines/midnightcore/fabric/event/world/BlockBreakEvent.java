package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.wallentines.midnightlib.event.Event;

public class BlockBreakEvent extends Event {

    private final ServerPlayer player;
    private final BlockPos position;
    private final BlockState state;

    private boolean cancelled = false;

    public BlockBreakEvent(ServerPlayer player, BlockPos position, BlockState state) {
        this.player = player;
        this.position = position;
        this.state = state;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public BlockPos getPosition() {
        return position;
    }

    public BlockState getState() {
        return state;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
