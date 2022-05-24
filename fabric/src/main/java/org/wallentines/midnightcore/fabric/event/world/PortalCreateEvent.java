package org.wallentines.midnightcore.fabric.event.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class PortalCreateEvent extends Event {

    private final ServerPlayer player;
    private final ServerLevel targetDimension;
    private final ServerLevel sourceDimension;

    private boolean cancelled = false;

    public PortalCreateEvent(ServerPlayer player, ServerLevel targetDimension, ServerLevel sourceDimension) {
        this.player = player;
        this.targetDimension = targetDimension;
        this.sourceDimension = sourceDimension;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLevel getTargetDimension() {
        return targetDimension;
    }

    public ServerLevel getSourceDimension() {
        return sourceDimension;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
