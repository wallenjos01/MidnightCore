package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.NumberSerializer;
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
import java.util.*;

public abstract class PluginMessageBroker {

    /**
     * The ID of the plugin message type used by this messenger
     */
    public static final Identifier MESSAGE_ID = new Identifier(MidnightCoreAPI.MOD_ID, "msg");

    protected static final byte REGISTER = 0;
    protected static final byte UNREGISTER = 1;
    protected static final byte REQUEST = 2;
    protected static final byte ONLINE = 3;


    protected SecretKey key;

    protected final List<PluginMessenger> messengers;
    private boolean isShutdown;

    protected PluginMessageBroker() {
        messengers = new ArrayList<>();
    }

    public void send(String channel, boolean encrypt, String namespace, ByteBuf message, boolean queue) {
        Packet out = new PluginMessageBroker.Packet(channel, encrypt, namespace, message);
        if(queue) out = out.queued();

        send(out);
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

        if(messenger.encrypt && key == null) {
            key = readKey(getKeyFile());
            if(key == null) {
                return;
            }
        }

        messengers.add(messenger);
    }

    public void shutdown() {
        if(isShutdown) return;
        isShutdown = true;

        onShutdown();
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
        protected final EnumSet<Flag> flags;
        protected byte systemChannel = -1;

        public Packet(String channel, boolean encrypt, String namespace, ByteBuf payload) {
            this.channel = channel;
            this.namespace = namespace;
            this.payload = payload;

            this.flags = EnumSet.noneOf(Flag.class);
            if(encrypt) flags.add(Flag.ENCRYPTED);
            if(namespace != null) flags.add(Flag.NAMESPACED);
        }

        public Packet(String channel, String namespace, ByteBuf payload, EnumSet<Flag> flags) {
            this.channel = channel;
            this.namespace = namespace;
            this.payload = payload;
            this.flags = flags;
        }

        Packet(byte systemChannel, ByteBuf payload) {
            this.channel = null;
            this.payload = payload;
            this.namespace = null;
            this.flags = EnumSet.of(Flag.SYSTEM);
            if(key != null) flags.add(Flag.ENCRYPTED);
            this.systemChannel = systemChannel;
        }

        public Packet queued() {

            flags.add(Flag.QUEUE);
            return this;
        }

        public Packet forFlags(EnumSet<Flag> flags) {
            if(flags.contains(Flag.SYSTEM)) {
                throw new IllegalArgumentException("SYSTEM packets may only be used internally!");
            }
            return new Packet(channel, namespace, payload, flags);
        }


        public Message toMessage(PluginMessenger messenger) {

            return new Message(messenger, channel, payload);
        }

        @Override
        public Identifier getId() {
            return MESSAGE_ID;
        }

        @Override
        public void write(ByteBuf buffer) {

            ByteBuf real;
            if(!flags.contains(Flag.ENCRYPTED) || buffer.hasArray()) {
                real = buffer;
            } else {
                real = Unpooled.buffer();
            }

            real.writeByte(Flag.pack(flags));
            int startIndex = real.writerIndex();
            if(flags.contains(Flag.NAMESPACED)) {
                PacketBufferUtil.writeUtf(real, namespace, 255);
            }

            if(flags.contains(Flag.SYSTEM)) {
                buffer.writeByte(systemChannel);
            } else {
                PacketBufferUtil.writeUtf(real, channel, 255);
            }

            if(payload == null) {
                PacketBufferUtil.writeVarInt(real, 0);
            } else {
                PacketBufferUtil.writeVarInt(real, payload.writerIndex());
                real.writeBytes(payload);
            }

            if(flags.contains(Flag.ENCRYPTED)) {
                real = encrypt(real, key, startIndex);
            }

            if(real != buffer) {
                buffer.writeBytes(real);
            }
        }

    }

    protected Packet readPacket(ByteBuf buffer) {

        EnumSet<Flag> flags = Flag.unpack(buffer.readByte());
        if(flags.contains(Flag.ENCRYPTED)) {
            buffer = decrypt(buffer, key);
        }
        String ns = null;
        if(flags.contains(Flag.NAMESPACED)) {
            ns = PacketBufferUtil.readUtf(buffer, 255);
        }

        String channel = null;
        byte systemChannel = -1;
        if(flags.contains(Flag.SYSTEM)) {
            systemChannel = buffer.readByte();
        } else {
            channel = PacketBufferUtil.readUtf(buffer, 255);
        }
        int length = PacketBufferUtil.readVarInt(buffer);

        Packet out = new Packet(channel, ns, buffer.readRetainedSlice(length), flags);
        out.systemChannel = systemChannel;

        return out;
    }

    public interface Factory {
        PluginMessageBroker create(MessengerModule module, ConfigSection config);
    }

}
