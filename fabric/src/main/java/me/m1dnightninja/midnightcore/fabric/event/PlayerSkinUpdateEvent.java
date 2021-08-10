package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.fabric.event.Event;
import net.minecraft.server.level.ServerPlayer;

public class PlayerSkinUpdateEvent extends Event {

    private final ServerPlayer player;
    private final Skin oldSkin;
    private Skin newSkin;

    private boolean cancelled = false;

    public PlayerSkinUpdateEvent(ServerPlayer player, Skin oldSkin, Skin newSkin) {
        this.player = player;
        this.oldSkin = oldSkin;
        this.newSkin = newSkin;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Skin getOldSkin() {
        return oldSkin;
    }

    public Skin getNewSkin() {
        return newSkin;
    }

    public void setNewSkin(Skin newSkin) {
        this.newSkin = newSkin;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
