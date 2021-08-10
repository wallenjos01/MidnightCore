package me.m1dnightninja.midnightcore.fabric.module.savepoint;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.module.savepoint.SavePointModule;
import net.minecraft.server.level.ServerPlayer;

public class SavePointLoadEvent extends Event {

    private final ServerPlayer player;
    private final SavePointModule module;

    private SavePointModule.SavePoint savePoint;
    private boolean cancelled = false;

    public SavePointLoadEvent(ServerPlayer player, SavePointModule module, SavePointModule.SavePoint savePoint) {
        this.player = player;
        this.module = module;
        this.savePoint = savePoint;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public SavePointModule getModule() {
        return module;
    }

    public SavePointModule.SavePoint getSavePoint() {
        return savePoint;
    }

    public void setSavePoint(SavePointModule.SavePoint savePoint) {
        this.savePoint = savePoint;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
