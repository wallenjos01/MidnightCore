package org.wallentines.mcore.savepoint;

import org.wallentines.mcore.Player;

public class SavepointLoadedEvent {

    private final Savepoint savepoint;
    private final Player player;

    public SavepointLoadedEvent(Savepoint savepoint, Player player) {
        this.savepoint = savepoint;
        this.player = player;
    }

    public Savepoint getSavepoint() {
        return savepoint;
    }

    public Player getPlayer() {
        return player;
    }

}
