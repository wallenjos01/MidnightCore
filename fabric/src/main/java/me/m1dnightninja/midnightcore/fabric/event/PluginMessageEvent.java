package me.m1dnightninja.midnightcore.fabric.event;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;


public class PluginMessageEvent extends Event {

    private final FriendlyByteBuf data;
    private final ServerPlayer source;

    public PluginMessageEvent(FriendlyByteBuf data, ServerPlayer source) {
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
