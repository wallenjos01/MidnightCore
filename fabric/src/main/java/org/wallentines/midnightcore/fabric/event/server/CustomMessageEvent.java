package org.wallentines.midnightcore.fabric.event.server;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class CustomMessageEvent extends Event {

    private final FriendlyByteBuf data;
    private final ServerPlayer source;

    public CustomMessageEvent(FriendlyByteBuf data, ServerPlayer source) {
        this.data = data;
        this.source = source;
    }

    public FriendlyByteBuf getData() {
        return data;
    }

    public ServerPlayer getSource() {
        return source;
    }

}
