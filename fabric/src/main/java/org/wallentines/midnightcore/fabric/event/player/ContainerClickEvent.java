package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.wallentines.midnightlib.event.Event;

public class ContainerClickEvent extends Event {

    private final Container inventory;
    private final AbstractContainerMenu handler;
    private final ServerPlayer player;
    private final int slot;
    private final ClickType action;
    private final int clickType;

    private boolean cancelled = false;

    public ContainerClickEvent(Container inventory, AbstractContainerMenu hand, ServerPlayer player, int slot, ClickType action, int clickType) {
        this.inventory = inventory;
        this.handler = hand;
        this.player = player;
        this.slot = slot;
        this.action = action;
        this.clickType = clickType;
    }

    public Container getInventory() {
        return inventory;
    }

    public AbstractContainerMenu getHandler() {
        return handler;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getSlot() {
        return slot;
    }

    public ClickType getAction() {
        return action;
    }

    public int getClickType() {
        return clickType;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public ItemStack getItem() {

        if(slot == AbstractContainerMenu.SLOT_CLICKED_OUTSIDE) {
            return handler.getCarried();
        }

        if(slot < 0 || handler.slots.size() <= slot) {
            return null;
        }

        return handler.getSlot(slot).getItem();

    }

}
