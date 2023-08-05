package org.wallentines.mcore.messaging;

import io.netty.buffer.ByteBuf;
import org.wallentines.midnightlib.registry.Identifier;

public interface Packet {

    Identifier getId();

    void write(ByteBuf buffer);

}
