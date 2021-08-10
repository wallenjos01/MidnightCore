package me.m1dnightninja.midnightcore.fabric.event;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;

public class PlayerLoginEvent extends Event {

    private final ServerPlayer ent;
    private final GameProfile prof;

    public PlayerLoginEvent(ServerPlayer ent, GameProfile prof) {
        this.ent = ent;
        this.prof = prof;
    }

    public ServerPlayer getPlayer() {
        return ent;
    }

    public GameProfile getProfile() {
        return prof;
    }
}
