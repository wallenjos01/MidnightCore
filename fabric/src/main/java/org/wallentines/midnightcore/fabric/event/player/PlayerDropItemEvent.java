package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightlib.event.Event;

public class PlayerDropItemEvent extends Event {

    private final ServerPlayer player;
    private final ItemStack item;

    private boolean cancelled;

    public PlayerDropItemEvent(ServerPlayer player, ItemStack item) {
        this.player = player;
        this.item = item;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ItemStack getItem() {
        return item;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
