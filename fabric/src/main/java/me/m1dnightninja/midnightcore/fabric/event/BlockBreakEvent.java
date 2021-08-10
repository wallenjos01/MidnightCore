package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.player.FabricPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBreakEvent extends Event {

    private final FabricPlayer player;
    private final BlockPos pos;
    private final BlockState state;

    private boolean cancelled = false;

    public BlockBreakEvent(FabricPlayer player, BlockPos pos, BlockState state) {
        this.player = player;
        this.pos = pos;
        this.state = state;
    }

    public FabricPlayer getPlayer() {
        return player;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
