package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.wallentines.midnightlib.event.Event;

public class BlockPlacedEvent extends Event {

    private final ServerPlayer player;
    private final BlockPos pos;
    private final BlockItem item;

    private final ItemStack itemStack;
    private BlockState placedState;

    public BlockPlacedEvent(ServerPlayer player, BlockPos pos, BlockItem item, ItemStack itemStack, BlockState placedState) {
        this.pos = pos;
        this.placedState = placedState;
        this.item = item;
        this.itemStack = itemStack;
        this.player = player;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockItem getItem() {
        return item;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public BlockState getPlacedState() {
        return placedState;
    }

    public void setPlacedState(BlockState placedState) {
        this.placedState = placedState;
    }
}
