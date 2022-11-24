package org.wallentines.midnightcore.fabric.event.client;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.wallentines.midnightlib.event.Event;

public class ClientCustomMessageEvent extends Event {

    private final ResourceLocation id;
    private final FriendlyByteBuf data;

    private boolean handled = false;

    public ClientCustomMessageEvent(ResourceLocation id, FriendlyByteBuf data) {
        this.id = id;
        this.data = data;
    }

    public ResourceLocation getId() {
        return id;
    }

    public FriendlyByteBuf getData() {
        return data;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
