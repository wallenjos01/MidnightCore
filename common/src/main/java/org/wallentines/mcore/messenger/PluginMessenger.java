package org.wallentines.mcore.messenger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.util.PacketBufferUtil;
import org.wallentines.midnightlib.event.EventHandler;
import org.wallentines.midnightlib.event.HandlerList;
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
import java.util.function.Consumer;

/**
 * A messenger which uses plugin messages to communicate across traditional Minecraft proxies
 */
public abstract class PluginMessenger implements Messenger {

    /**
     * The ID of the plugin message type used by this messenger
     */
    public static final Identifier MESSAGE_ID = new Identifier(MidnightCoreAPI.MOD_ID, "msg");

    /**
     * The message channel used to register a server on the proxy
     */
    public static final String REGISTER_CHANNEL = "__mcore_register";

    private final Map<String, HandlerList<Message>> handlers;
    private final SecretKey encryptionKey;
    private final EnumSet<Flag> flags;
    protected final String namespace;

    protected final Queue<Packet> queue = new ArrayDeque<>();

    protected PluginMessenger(SecretKey encryptionKey, String namespace) {
        this.handlers = new HashMap<>();
        this.encryptionKey = encryptionKey;
        this.namespace = namespace;

        List<Flag> fl = new ArrayList<>();
        if(encryptionKey != null) fl.add(Flag.ENCRYPTED);
        if(namespace != null) fl.add(Flag.NAMESPACED);

        this.flags = EnumSet.copyOf(fl);

        registerHandler(pck -> {

            HandlerList<Message> list = handlers.get(pck.channel);

            if(list == null) {
                return;
            }

            if(!Objects.equals(pck.namespace, namespace)) {
                MidnightCoreAPI.LOGGER.warn("Received a message with mismatched namespace! (" + pck.namespace + ")");
                return;
            }

            list.invoke(pck.toMessage());
        });
    }

    protected abstract void registerHandler(Consumer<Packet> handler);

    @Override
    public void unsubscribe(Object listener, String channel) {
        handlers.computeIfPresent(channel, (k,v) -> {
            v.unregisterAll(listener);
            return v;
        });
    }

    @Override
    public void subscribe(Object listener, String channel, EventHandler<Message> handler) {
        if(handlers.containsKey(channel)) {
            MidnightCoreAPI.LOGGER.warn("Re-registered messenger channel " + channel);
        }
        handlers.compute(channel, (k,v) -> {
            if(v == null) v = new HandlerList<>();
            v.register(listener, handler);
            return v;
        });
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
        QUEUE(0b00000100);

        final byte mask;

        Flag(int mask) {
            this.mask = (byte) mask;
        }

        public static EnumSet<Flag> unpack(byte flags) {

            List<Flag> flag = new ArrayList<>();
            for(Flag f : values()) {
                if((flags & f.mask) == 1) flag.add(ENCRYPTED);
            }

            return EnumSet.copyOf(flag);
        }

        public static byte pack(EnumSet<Flag> flags) {

            byte out = 0b00000000;
            for(Flag f : flags) {
                out |= f.mask;
            }
            return out;
        }

    }

    protected class Packet implements org.wallentines.mcore.pluginmsg.Packet {

        protected final String channel;
        protected final String namespace;
        protected final EnumSet<Flag> flags;
        protected final ByteBuf payload;
        public Packet(String channel, String namespace, EnumSet<Flag> flags, ByteBuf payload) {
            this.channel = channel;
            this.namespace = namespace;
            this.flags = flags;
            this.payload = payload;
        }

        public Packet(String channel, String namespace, ByteBuf payload) {
            this(channel, namespace, EnumSet.copyOf(PluginMessenger.this.flags), payload);
        }


        public Packet queued() {

            EnumSet<Flag> outFlags = EnumSet.copyOf(flags);
            outFlags.add(Flag.QUEUE);

            return new Packet(channel, namespace, outFlags, payload);
        }

        public Message toMessage() {

            return new Message(PluginMessenger.this, channel, payload);
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
            PacketBufferUtil.writeUtf(real, channel, 255);
            if(payload == null) {
                PacketBufferUtil.writeVarInt(real, 0);
            } else {
                PacketBufferUtil.writeVarInt(real, payload.writerIndex());
                real.writeBytes(payload);
            }

            if(flags.contains(Flag.ENCRYPTED)) {
                real = encrypt(real, encryptionKey, startIndex);
            }

            if(real != buffer) {
                buffer.writeBytes(real);
            }
        }

    }

    protected Packet readPacket(ByteBuf buffer) {

        EnumSet<Flag> flags = Flag.unpack(buffer.readByte());
        if(flags.contains(Flag.ENCRYPTED)) {
            buffer = decrypt(buffer, encryptionKey);
        }
        String ns = null;
        if(flags.contains(Flag.NAMESPACED)) {
            ns = PacketBufferUtil.readUtf(buffer, 255);
        }

        String channel = PacketBufferUtil.readUtf(buffer, 255);
        int length = PacketBufferUtil.readVarInt(buffer);

        return new Packet(channel, ns, flags, buffer.readRetainedSlice(length));
    }

}
