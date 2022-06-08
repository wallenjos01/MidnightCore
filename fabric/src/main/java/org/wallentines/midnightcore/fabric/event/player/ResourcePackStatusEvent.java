package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.event.Event;

public class ResourcePackStatusEvent extends Event {

    private final ServerPlayer player;
    private final MPlayer.ResourcePackStatus status;

    public ResourcePackStatusEvent(ServerPlayer player, MPlayer.ResourcePackStatus status) {
        this.player = player;
        this.status = status;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public MPlayer.ResourcePackStatus getStatus() {
        return status;
    }
}
