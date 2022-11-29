import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.midnightcore.common.module.messaging.PacketBufferUtils;

public class TestPacketBuffer {

    @Test
    public void testVarInt() {

        ByteBuf buf = Unpooled.buffer();

        PacketBufferUtils.writeVarInt(buf, 1);
        PacketBufferUtils.writeVarInt(buf, 12732789);

        FriendlyByteBuf fbuf = new FriendlyByteBuf(buf);

        Assertions.assertEquals(1, fbuf.readVarInt());
        Assertions.assertEquals(12732789, fbuf.readVarInt());

        FriendlyByteBuf fbuf2 = new FriendlyByteBuf(Unpooled.buffer());

        fbuf2.writeVarInt(345);
        fbuf2.writeVarInt(198743);

        ByteBuf buf2 = fbuf2.asByteBuf();

        Assertions.assertEquals(345, PacketBufferUtils.readVarInt(buf2));
        Assertions.assertEquals(198743, PacketBufferUtils.readVarInt(buf2));

    }

    @Test
    public void testUtf() {

        ByteBuf buf = Unpooled.buffer();

        PacketBufferUtils.writeUtf(buf, "Hello");
        PacketBufferUtils.writeUtf(buf, "According to all known laws of aviation, there is no way a bee should be able to fly.");

        FriendlyByteBuf fbuf = new FriendlyByteBuf(buf);

        Assertions.assertEquals("Hello", fbuf.readUtf());
        Assertions.assertEquals("According to all known laws of aviation, there is no way a bee should be able to fly.", fbuf.readUtf());


        FriendlyByteBuf fbuf2 = new FriendlyByteBuf(buf);

        fbuf2.writeUtf("World");
        fbuf2.writeUtf("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");

        ByteBuf buf2 = fbuf2.asByteBuf();

        Assertions.assertEquals("World", PacketBufferUtils.readUtf(buf2));
        Assertions.assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.", PacketBufferUtils.readUtf(buf2));

    }

}
