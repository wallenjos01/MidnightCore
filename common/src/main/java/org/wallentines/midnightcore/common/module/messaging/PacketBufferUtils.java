package org.wallentines.midnightcore.common.module.messaging;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.nio.charset.StandardCharsets;

public class PacketBufferUtils  {

    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

/*    public static int readVarInt(ByteBuf buffer) {
        int value = 0;
        int position = 0;

        byte currentByte;

        while (true) {
            currentByte = buffer.readByte();
            value |= (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;
            if (position >= 32) throw new IllegalArgumentException("Attempt to read a VarInt which is larger than 32-bits!");
        }

        return value;
    }*/

    public static int readVarInt(ByteBuf buffer) {

        int value = 0;
        int position = 0;

        byte currentByte;

        do {

            currentByte = buffer.readByte();
            value |= (currentByte & SEGMENT_BITS) << position;

            position += 7;

            if(position >= 32) {
                throw new IllegalArgumentException("Attempt to read a VarInt which is larger than 32-bits!");
            }

        } while((currentByte & CONTINUE_BIT) == CONTINUE_BIT);

        return value;
    }


    public static void writeVarInt(ByteBuf buffer, int i) {

        while((i & ~SEGMENT_BITS) != 0) {
            buffer.writeByte((i & SEGMENT_BITS) | CONTINUE_BIT);
            i >>>= 7;
        }
        buffer.writeByte(i);
    }

    public static void writeUtf(ByteBuf output, String string) {
        writeUtf(output, string, 32767);
    }

    public static void writeUtf(ByteBuf output, String string, int max) {

        if (string.length() > max) {
            throw new IllegalArgumentException("Attempt to write a UTF String which is longer than the specified maximum! (" + max + ")");
        }

        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        int actualMax = max * 3;
        if (bs.length > actualMax) {
            throw new IllegalArgumentException("Attempt to write a UTF String which is longer than the allowed maximum! (" + actualMax + ")");
        }

        writeVarInt(output, bs.length);
        output.writeBytes(bs);
    }

    public static String readUtf(ByteBuf input) {
        return readUtf(input, 32767);
    }

    public static String readUtf(ByteBuf input, int max) {

        int actualMax = max * 3;
        int length = readVarInt(input);
        if (length > actualMax) {
            throw new DecoderException("Attempt to read a UTF String which is longer than the allowed maximum! (" + length + ")");
        }
        if (length < 0) {
            throw new DecoderException("Attempt to read a UTF String with negative length!");
        }

        String string = input.toString(input.readerIndex(), length, StandardCharsets.UTF_8);
        input.readerIndex(input.readerIndex() + length);

        if (string.length() > max) {
            throw new DecoderException("Attempt to read a UTF String which is longer than the specified maximum! (" + string.length() + ")");
        }

        return string;
    }

}
