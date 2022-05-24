package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.player.Location;
import org.wallentines.midnightlib.event.Event;

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
