package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;

public class PlayerXPEvent {

    private final ServerPlayer player;
    private final int prevLevel;
    private final int currentLevel;
    private final int prevXP;
    private final int currentXP;

    private boolean cancelled;

    public PlayerXPEvent(ServerPlayer player, int prevLevel, int prevXP) {
        this.player = player;
        this.prevLevel = prevLevel;
        this.currentLevel = player.experienceLevel;
        this.prevXP = prevXP;
        this.currentXP = player.totalExperience;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getPreviousLevel() {
        return prevLevel;
    }

    public int getLevel() {
        return currentLevel;
    }

    public int getPreviousTotalXP() {
        return prevXP;
    }

    public int getTotalXP() {
        return currentXP;
    }

    public int getGainedXP() {
        return currentXP - prevXP;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
