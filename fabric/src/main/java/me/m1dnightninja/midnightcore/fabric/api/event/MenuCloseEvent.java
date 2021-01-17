package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MenuCloseEvent extends Event {

    private final ServerPlayer player;
    private final AbstractContainerMenu handler;

    public MenuCloseEvent(ServerPlayer player, AbstractContainerMenu handler) {
        this.player = player;
        this.handler = handler;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public AbstractContainerMenu getMenu() {
        return handler;
    }
}