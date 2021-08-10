package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.fabric.event.Event;

public class PortalCreateEvent extends Event {

    private final MPlayer creator;
    private final MIdentifier targetDimension;
    private final MIdentifier sourceDimension;

    private boolean cancelled = false;

    public PortalCreateEvent(MPlayer creator, MIdentifier targetDimension, MIdentifier sourceDimension) {
        this.creator = creator;
        this.targetDimension = targetDimension;
        this.sourceDimension = sourceDimension;
    }

    public MPlayer getCreator() {
        return creator;
    }

    public MIdentifier getTargetDimension() {
        return targetDimension;
    }

    public MIdentifier getSourceDimension() {
        return sourceDimension;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
