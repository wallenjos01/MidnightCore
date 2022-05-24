package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class PlayerChangeDimensionEvent extends Event {

    private final ServerPlayer player;
    private final ServerLevel oldLevel;
    private final ServerLevel newLevel;

    private boolean cancelled;

    public PlayerChangeDimensionEvent(ServerPlayer player, ServerLevel oldLevel, ServerLevel newLevel) {
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ServerLevel getOldLevel() {
        return oldLevel;
    }

    public ServerLevel getNewLevel() {
        return newLevel;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
