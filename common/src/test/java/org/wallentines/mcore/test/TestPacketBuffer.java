package org.wallentines.mcore.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.util.PacketBufferUtil;

public class TestPacketBuffer {

    @Test
    public void testPacketBufferUtil() {

        ByteBuf buf = Unpooled.buffer();

        PacketBufferUtil.writeVarInt(buf, 37);
        PacketBufferUtil.writeUtf(buf, "Hello, World");


        buf.resetReaderIndex();

        Assertions.assertEquals(37, PacketBufferUtil.readVarInt(buf));
        Assertions.assertEquals("Hello, World", PacketBufferUtil.readUtf(buf));

    }

}
