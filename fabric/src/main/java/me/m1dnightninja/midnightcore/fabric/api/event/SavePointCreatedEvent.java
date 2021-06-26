package me.m1dnightninja.midnightcore.fabric.api.event;

import me.m1dnightninja.midnightcore.fabric.event.Event;
import me.m1dnightninja.midnightcore.fabric.module.SavePointModule;
import net.minecraft.server.level.ServerPlayer;

public class SavePointCreatedEvent extends Event {

    private final ServerPlayer player;
    private final SavePointModule module;
    private final SavePointModule.SavePoint savePoint;

    public SavePointCreatedEvent(ServerPlayer player, SavePointModule module, SavePointModule.SavePoint savePoint) {
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
}
