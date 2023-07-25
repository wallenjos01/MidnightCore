package org.wallentines.mcore.savepoint;

import org.wallentines.mcore.Player;

import java.util.EnumSet;

public class SavepointCreatedEvent {

    private final Savepoint savepoint;
    private final Player player;
    private final EnumSet<SavepointModule.SaveFlag> flags;

    public SavepointCreatedEvent(Savepoint savepoint, Player player, EnumSet<SavepointModule.SaveFlag> flags) {
        this.savepoint = savepoint;
        this.player = player;
        this.flags = flags;
    }

    public Savepoint getSavepoint() {
        return savepoint;
    }

    public Player getPlayer() {
        return player;
    }

    public EnumSet<SavepointModule.SaveFlag> getFlags() {
        return flags;
    }
}
