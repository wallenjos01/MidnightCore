package org.wallentines.mcore.util;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.nio.charset.StandardCharsets;

/**
 * A utility for reading and writing integers and Strings to buffers in a Mojang-compatible format
 */
public class PacketBufferUtil {

    // How many bits are in each segment of a variable-length integer (VarInt)
    private static final int SEGMENT_BITS = 0b01111111;

    // If this bit is set when reading a VarInt, then the next byte should be read as part of the VarInt
    private static final int CONTINUE_BIT = 0b10000000;


    /**
     * Reads a variable-length integer (VarInt) from a buffer
     * @param buffer The buffer to read from
     * @return An integer
     */
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

    /**
     * Writes a variable-length integer (VarInt) to a buffer
     * @param buffer The buffer to write to
     * @param value The integer to write
     */
    public static void writeVarInt(ByteBuf buffer, int value) {

        while((value & ~SEGMENT_BITS) != 0) {
            buffer.writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);
            value >>>= 7;
        }
        buffer.writeByte(value);
    }

    /**
     * Writes a UTF-encoded String to a buffer
     * @param buffer The buffer to write to
     * @param string The string to write
     * @throws IllegalArgumentException If the specified string is longer than 32767 bytes
     */
    public static void writeUtf(ByteBuf buffer, String string) {
        writeUtf(buffer, string, 32767);
    }

    /**
     * Writes a UTF-encoded String to a buffer, ensuring a specified maximum length
     * @param buffer The buffer to write to
     * @param string The string to write
     * @param max The maximum number of characters to write
     * @throws IllegalArgumentException If the specified string is longer than the specified maximum
     */
    public static void writeUtf(ByteBuf buffer, String string, int max) {

        if (string.length() > max) {
            throw new IllegalArgumentException("Attempt to write a UTF String which is longer than the specified maximum! (" + max + ")");
        }

        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        int actualMax = max * 3;
        if (bs.length > actualMax) {
            throw new IllegalArgumentException("Attempt to write a UTF String which is longer than the allowed maximum! (" + actualMax + ")");
        }

        writeVarInt(buffer, bs.length);
        buffer.writeBytes(bs);
    }

    /**
     * Reads a UTF-encoded String from a buffer
     * @param input The buffer to read
     * @return A decoded String
     */
    public static String readUtf(ByteBuf input) {
        return readUtf(input, 32767);
    }

    /**
     * Reads a UTF-encoded String from a buffer, ensuring a specified maximum length
     * @param input The buffer to read from
     * @param max The maximum number of bytes which can be read
     * @return A decoded String
     * @throws DecoderException If the string is too long
     */
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
