package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.Serializer;
import org.wallentines.midnightlib.registry.Identifier;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public abstract class PluginMessageBroker {

    /**
     * The ID of the plugin message type used by this messenger
     */
    public static final Identifier MESSAGE_ID = new Identifier(MidnightCoreAPI.MOD_ID, "msg");

    protected static final byte REGISTER = 1;
    protected static final byte UNREGISTER = 2;
    protected static final byte REQUEST = 3;
    protected static final byte ONLINE = 4;


    protected SecretKey key;

    protected final List<PluginMessenger> messengers;
    private boolean isShutdown;

    protected PluginMessageBroker(boolean encrypt) {
        messengers = new ArrayList<>();
        if(encrypt) key = readKey(getKeyFile());
    }

    public void send(String channel, String namespace, int ttl, ByteBuf message) {
        send(new PluginMessageBroker.Packet(channel, key != null, namespace, ttl, message));
    }

    protected abstract void send(Packet packet);

    protected void handle(Packet packet) {

        for(PluginMessenger msg : messengers) {
            if(Objects.equals(msg.namespace, packet.namespace)) {
                msg.handle(packet);
            }
        }
    }

    public void register(PluginMessenger messenger) {
        messengers.add(messenger);
    }

    public void shutdown() {
        if(isShutdown) return;
        isShutdown = true;

        onShutdown();
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    protected abstract void onShutdown();

    private static ByteBuf decrypt(ByteBuf buffer, SecretKey key) {

        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            if(buffer.hasArray()) {
                cipher.update(buffer.array(), buffer.readerIndex(), buffer.readableBytes(), buffer.array(), buffer.readerIndex());
                return buffer;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buffer.readBytes(bos, buffer.readableBytes());

            return Unpooled.wrappedBuffer(cipher.doFinal(bos.toByteArray()));

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | ShortBufferException | BadPaddingException | IOException e) {
            throw new RuntimeException("Unable to decrypt message!", e);
        }
    }

    private static ByteBuf encrypt(ByteBuf buffer, SecretKey key, int start) {

        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            if(buffer.hasArray()) {
                cipher.update(buffer.array(), start, buffer.writerIndex() - start, buffer.array(), start);
                return buffer;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buffer.readerIndex(start);
            buffer.readBytes(bos, buffer.writerIndex() - start);

            return Unpooled.wrappedBuffer(cipher.doFinal(bos.toByteArray()));

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                 IllegalBlockSizeException | ShortBufferException | BadPaddingException | IOException e) {
            throw new RuntimeException("Unable to encrypt message!", e);
        }
    }

    protected abstract File getKeyFile();

    protected static SecretKey readKey(File file) {
        try(FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int read;
            while((read = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }

            return new SecretKeySpec(bos.toByteArray(), "AES");
        } catch (IOException ex) {
            MidnightCoreAPI.LOGGER.error("Unable to read encryption key!");
            return null;
        }
    }

    /**
     * Flags encoded in message packets
     */
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

        public static final Serializer<EnumSet<Flag>> SERIALIZER = Serializer.BYTE.map(Flag::pack, Flag::unpack);

    }

    protected class Packet implements org.wallentines.mcore.pluginmsg.Packet {

        protected final String channel;
        protected final ByteBuf payload;
        protected final String namespace;
        protected final Instant sent;
        protected final boolean encrypt;
        protected final int ttl;
        protected final byte systemChannel;

        public Packet(String channel, boolean encrypt, String namespace, int ttl, ByteBuf payload) {
            this(channel, encrypt, namespace, ttl, payload, Instant.now(Clock.systemUTC()));
        }

        private Packet(String channel, boolean encrypt, String namespace, int ttl, ByteBuf payload, Instant sent) {
            this.channel = channel;
            this.sent = sent;
            this.payload = payload;
            this.namespace = namespace;
            this.encrypt = encrypt;
            this.ttl = ttl;
            this.systemChannel = -1;
        }


        private Packet(byte systemChannel, boolean encrypt, ByteBuf payload, Instant sent) {
            this.channel = null;
            this.sent = sent;
            this.payload = payload;
            this.namespace = null;
            this.ttl = 0;
            this.encrypt = encrypt;
            this.systemChannel = systemChannel;
        }

        Packet(byte systemChannel, ByteBuf payload) {
            this(systemChannel, key != null, payload, Instant.now(Clock.systemUTC()));
        }

        public boolean isSystemMessage() {
            return systemChannel > 0;
        }

        public Message toMessage(PluginMessenger messenger) {

            return new Message(messenger, channel, payload);
        }

        public Packet encrypted(boolean encrypt) {
            if(this.encrypt == encrypt) return this;
            return new Packet(channel, encrypt, namespace, ttl, payload, sent);
        }

        public EnumSet<Flag> getFlags() {
            EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
            if(namespace != null) flags.add(Flag.NAMESPACED);
            if(encrypt) flags.add(Flag.ENCRYPTED);
            if(ttl > 0) flags.add(Flag.QUEUE);
            if(systemChannel > 0) flags.add(Flag.SYSTEM);
            return flags;
        }

        public boolean isExpired() {
            return ttl > 0 && sent.plus(ttl, ChronoUnit.MILLIS).isBefore(Instant.now(Clock.systemUTC()));
        }

        @Override
        public Identifier getId() {
            return MESSAGE_ID;
        }

        @Override
        public void write(ByteBuf buffer) {


            // Setup encryption buffer if necessary
            ByteBuf real;
            if(!encrypt || buffer.hasArray()) {
                real = buffer;
            } else {
                real = Unpooled.buffer();
            }


            // Flags
            real.writeByte(Flag.pack(getFlags()));
            int startIndex = real.writerIndex();

            // Sent
            real.writeLong(sent.toEpochMilli());

            // TTL
            if(ttl > 0) {
                real.writeInt(ttl);
            }

            // Namespace
            if(namespace != null) {
                PacketBufferUtil.writeUtf(real, namespace, 255);
            }

            // Channel
            if(systemChannel > 0) {
                buffer.writeByte(systemChannel);
            } else {
                if(channel == null) {
                    PacketBufferUtil.writeVarInt(real, 0);
                } else {
                    PacketBufferUtil.writeUtf(real, channel, 255);
                }
            }

            // Payload
            if(payload == null) {
                PacketBufferUtil.writeVarInt(real, 0);
            } else {
                PacketBufferUtil.writeVarInt(real, payload.writerIndex());
                real.writeBytes(payload);
            }

            // Encryption
            if(encrypt) {
                real = encrypt(real, key, startIndex);
            }

            if(real != buffer) {
                buffer.writeBytes(real);
            }
        }

    }

    protected Packet readPacket(ByteBuf buffer) {

        // Flags
        EnumSet<Flag> flags = Flag.unpack(buffer.readByte());

        // Decrypt
        boolean encrypt = flags.contains(Flag.ENCRYPTED);
        if(encrypt) {
            buffer = decrypt(buffer, key);
        }

        // Timestamp
        Instant sent = Instant.ofEpochSecond(buffer.readLong());

        // TTL
        int ttl = 0;
        if (flags.contains(Flag.QUEUE)) {
            ttl = buffer.readInt();
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

        Packet out;
        if(systemChannel > 0) {
            out = new Packet(systemChannel, encrypt, payload, sent);
        } else {
            out = new Packet(channel, encrypt, ns, ttl, payload, sent);
        }

        return out;
    }

    public interface Factory {
        PluginMessageBroker create(MessengerModule module, ConfigSection config);
    }

}
