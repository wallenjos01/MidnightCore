package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightlib.event.Event;

public class BlockPlaceEvent extends Event {

    private final ServerPlayer player;
    private final BlockPos pos;
    private final BlockItem item;

    private boolean cancelled = false;

    public BlockPlaceEvent(ServerPlayer player, BlockPos pos, BlockItem item) {
        this.player = player;
        this.pos = pos;
        this.item = item;
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

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
