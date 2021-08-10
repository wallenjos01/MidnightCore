package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;

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
}
