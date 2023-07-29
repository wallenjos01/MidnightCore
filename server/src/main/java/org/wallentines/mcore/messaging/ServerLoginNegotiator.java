package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.UUID;
import java.util.function.Consumer;

public abstract class ServerLoginNegotiator {

    private final UUID uid;
    private final String name;

    protected ServerLoginNegotiator(UUID uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public UUID getPlayerUUID() {
        return uid;
    }
    public String getPlayerName() { return name; }

    public abstract void sendPacket(Identifier id, ByteBuf data, Consumer<ByteBuf> response);

}
