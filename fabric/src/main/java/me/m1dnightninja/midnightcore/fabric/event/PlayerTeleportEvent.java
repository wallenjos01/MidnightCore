package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.api.player.Location;
import net.minecraft.server.level.ServerPlayer;

public class PlayerTeleportEvent extends Event {

    private final ServerPlayer player;
    private final Location from;
    private final Location to;

    private boolean cancelled = false;

    public PlayerTeleportEvent(ServerPlayer player, Location from, Location to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
