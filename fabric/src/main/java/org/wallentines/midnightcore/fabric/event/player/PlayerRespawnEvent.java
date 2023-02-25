package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class PlayerRespawnEvent {

    private final ServerPlayer player;
    private Vec3 position;
    private ServerLevel level;

    public PlayerRespawnEvent(ServerPlayer player, Vec3 position, ServerLevel level) {
        this.player = player;
        this.position = position;
        this.level = level;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Vec3 getPosition() {
        return position;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public void setLevel(ServerLevel level) {
        this.level = level;
    }
}
