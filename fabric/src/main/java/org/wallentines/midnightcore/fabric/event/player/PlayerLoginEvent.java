package org.wallentines.midnightcore.fabric.event.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class PlayerLoginEvent extends Event {

    private final ServerPlayer player;
    private final GameProfile profile;

    public PlayerLoginEvent(ServerPlayer player, GameProfile profile) {
        this.player = player;
        this.profile = profile;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public GameProfile getProfile() {
        return profile;
    }
}
