package org.wallentines.midnightcore.api.module.savepoint;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.event.Event;

public class SavepointCreatedEvent extends Event {

    private final Savepoint savepoint;
    private final MPlayer player;
    private boolean cancelled = false;

    public SavepointCreatedEvent(Savepoint savepoint, MPlayer player) {
        this.savepoint = savepoint;
        this.player = player;
    }

    public Savepoint getSavepoint() {
        return savepoint;
    }

    public MPlayer getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
