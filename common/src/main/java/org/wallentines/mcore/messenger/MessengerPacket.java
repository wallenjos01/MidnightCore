package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.pluginmsg.Packet;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.Identifier;
import org.wallentines.smi.Message;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;

public class MessengerPacket implements Packet {

    // The ID of the plugin message type used by this messenger
    public static final Identifier MESSAGE_ID = new Identifier(MidnightCoreAPI.MOD_ID, "msg");


    // Packet flags
    public enum Flag {
        ENCRYPTED(0b00000001),
        NAMESPACED(0b00000010),
        QUEUE(0b00000100),
        SYSTEM(0b00001000);

        final byte mask;

        Flag(int mask) {
            this.mask = (byte) mask;
        }

        public static EnumSet<Flag> unpack(byte flags) {

            EnumSet<Flag> out = EnumSet.noneOf(Flag.class);
            for(Flag f : values()) {
                if((flags & f.mask) == f.mask) out.add(f);
            }

            return out;
        }

        public static byte pack(EnumSet<Flag> flags) {

            byte out = 0b00000000;
            for(Flag f : flags) {
                out |= f.mask;
            }
            return out;
        }

        public static final Serializer<EnumSet<Flag>> SERIALIZER = Serializer.BYTE.flatMap(Flag::pack, Flag::unpack);
    }



    protected final String channel;
    protected final ByteBuf payload;
    protected final String namespace;
    protected final Instant sent;
    protected final BufCipher cipher;
    protected final long ttl;
    protected final byte systemChannel;

    public MessengerPacket(String channel, ByteBuf payload, BufCipher cipher, String namespace, long ttl) {
        this(channel, payload, cipher, namespace, ttl, Instant.now(Clock.systemUTC()));
    }

    public MessengerPacket(String channel, ByteBuf payload, BufCipher cipher, String namespace, long ttl, Instant sent) {
        this.channel = channel;
        this.payload = payload == null ? null : payload.duplicate();
        this.sent = sent;
        this.cipher = cipher;
        this.namespace = namespace;
        this.ttl = ttl;
        this.systemChannel = -1;
    }

    MessengerPacket(byte systemChannel, ByteBuf payload, BufCipher cipher) {
        this(systemChannel, payload, cipher, Instant.now(Clock.systemUTC()));
    }

    MessengerPacket(byte systemChannel, ByteBuf payload, BufCipher cipher, Instant sent) {
        this.channel = null;
        this.payload = payload == null ? null : payload.duplicate();
        this.sent = sent;
        this.namespace = null;
        this.ttl = 0;
        this.cipher = cipher;
        this.systemChannel = systemChannel;
    }

    private MessengerPacket(MessengerPacket packet, BufCipher cipher) {
        this.channel = packet.channel;
        this.payload = packet.payload == null ? null : packet.payload.duplicate();
        this.sent = packet.sent;
        this.namespace = packet.namespace;
        this.ttl = packet.ttl;
        this.cipher = cipher;
        this.systemChannel = packet.systemChannel;
    }

    public boolean isSystemMessage() {
        return systemChannel > 0;
    }

    public MessengerPacket withCipher(BufCipher cipher) {
        return new MessengerPacket(this, cipher);
    }

    public EnumSet<Flag> getFlags() {
        EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
        if(namespace != null) flags.add(Flag.NAMESPACED);
        if(cipher != null) flags.add(Flag.ENCRYPTED);
        if(ttl > 0) flags.add(Flag.QUEUE);
        if(systemChannel > 0) flags.add(Flag.SYSTEM);
        return flags;
    }

    public boolean isExpired() {
        return ttl > 0 && sent.plus(ttl, ChronoUnit.MILLIS).isBefore(Instant.now(Clock.systemUTC()));
    }

    public Message toMessage() {

        ByteBuffer buffer;
        if(payload != null && payload.readableBytes() > 0) {
            buffer = ByteBuffer.allocate(payload.readableBytes());
            payload.readBytes(buffer);
            buffer.flip();
        } else {
            buffer = null;
        }

        return new Message(channel, buffer);
    }

    @Override
    public Identifier getId() {
        return MESSAGE_ID;
    }

    @Override
    public void write(ByteBuf buffer) {

        // Flags
        byte flags = Flag.pack(getFlags());
        buffer.writeByte(flags);

        ByteBuf real = Unpooled.buffer();

        // Sent
        real.writeLong(sent.getEpochSecond());

        // TTL
        if(ttl > 0) {
            real.writeLong(ttl);
        }

        // Namespace
        if(namespace != null) {
            PacketBufferUtil.writeUtf(real, namespace, 255);
        }

        // Channel
        if(systemChannel != -1) {
            real.writeByte(systemChannel);
        } else {
            PacketBufferUtil.writeUtf(real, channel, 255);
        }

        // Payload
        if(payload == null) {
            PacketBufferUtil.writeVarInt(real, 0);
        } else {
            int length = payload.readableBytes();
            PacketBufferUtil.writeVarInt(real, length);
            if (length > 0) {
                real.writeBytes(PacketBufferUtil.getBytes(payload));
            }
        }

        // Encryption
        if(cipher != null) {
            cipher.encrypt(real, buffer);
        } else {
            buffer.writeBytes(real);
        }
    }


    public static MessengerPacket readPacket(ByteBuf buffer, BufCipher cipher) {

        // Flags
        EnumSet<Flag> flags = Flag.unpack(buffer.readByte());

        // Decrypt
        boolean encrypt = flags.contains(Flag.ENCRYPTED);
        if(encrypt) {
            if(cipher == null) {
                throw new IllegalStateException("Received an encrypted packet without a key to decrypt it! Please put a AES-128 key called messenger.key in the MidnightCore folder!");
            }

            ByteBuf decrypted = Unpooled.buffer(cipher.getOutputLength(buffer.readableBytes()));
            cipher.decrypt(buffer, decrypted);
            buffer = decrypted;
        }

        // Timestamp
        long time = buffer.readLong();
        Instant sent = Instant.ofEpochSecond(time);

        // TTL
        long ttl = 0;
        if (flags.contains(Flag.QUEUE)) {
            ttl = buffer.readLong();
        }

        // Namespace
        String ns = null;
        if(flags.contains(Flag.NAMESPACED)) {
            ns = PacketBufferUtil.readUtf(buffer, 255);
        }

        // Channel
        String channel = null;
        byte systemChannel = 0;
        if(flags.contains(Flag.SYSTEM)) {
            systemChannel = buffer.readByte();
        } else {
            channel = PacketBufferUtil.readUtf(buffer, 255);
        }

        // Payload
        int length = PacketBufferUtil.readVarInt(buffer);
        ByteBuf payload = buffer.readRetainedSlice(length);

        MessengerPacket out;
        if(systemChannel > 0) {
            out = new MessengerPacket(systemChannel, payload, cipher, sent);
        } else {
            out = new MessengerPacket(channel, payload, cipher, ns, ttl, sent);
        }

        return out;
    }

}
