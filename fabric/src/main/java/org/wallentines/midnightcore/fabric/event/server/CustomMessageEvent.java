package org.wallentines.midnightcore.fabric.event.server;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.wallentines.midnightlib.event.Event;

public class CustomMessageEvent extends Event {

    private final ResourceLocation id;
    private final FriendlyByteBuf data;
    private final ServerPlayer source;
    private boolean handled = false;

    public CustomMessageEvent(ResourceLocation id, FriendlyByteBuf data, ServerPlayer source) {
        this.id = id;
        this.data = data;
        this.source = source;
    }

    public ResourceLocation getPacketId() {
        return id;
    }

    public FriendlyByteBuf getData() {
        //FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(data.capacity()));
        //buf.writeBytes(data);

        return data;
    }

    public ServerPlayer getSource() {
        return source;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
