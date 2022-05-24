package org.wallentines.midnightcore.fabric.event.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.fabric.player.FabricPlayer;
import org.wallentines.midnightcore.fabric.util.ConversionUtil;
import org.wallentines.midnightlib.event.Event;

public class PlayerJoinEvent extends Event {

    private final ServerPlayer player;
    private Component joinMessage;

    public PlayerJoinEvent(ServerPlayer player, Component joinMessage) {
        this.player = player;
        this.joinMessage = joinMessage;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Component getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(Component joinMessage) {
        this.joinMessage = joinMessage;
    }

    public void setJoinMessage(MComponent joinMessage) {
        this.joinMessage = ConversionUtil.toComponent(joinMessage);
    }
}
