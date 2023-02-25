package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class AdvancementEvent extends Event {

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
