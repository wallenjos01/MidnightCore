package org.wallentines.mcore.event;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementEvent {

    private final ServerPlayer player;
    private final Advancement advancement;

    public AdvancementEvent(ServerPlayer player, Advancement advancement) {
        this.player = player;
        this.advancement = advancement;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Advancement getAdvancement() {
        return advancement;
    }
}
