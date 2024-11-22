package org.wallentines.mcore.messenger;

import io.netty.buffer.Unpooled;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.smi.Message;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class PluginMessageBroker {


    // System packet types
    protected static final byte REGISTER = 1;
    protected static final byte UNREGISTER = 2;
    protected static final byte REQUEST = 3;
    protected static final byte ONLINE = 4;


    protected final List<PluginMessenger> messengers;
    protected final BufCipher cipher;

    public PluginMessageBroker(Path keyFile) {
        this.messengers = new ArrayList<>();
        if(keyFile == null) {
            this.cipher = null;
        } else {
            BufCipher tempCipher = null;
            try {
                tempCipher = new BufCipher(readKey(keyFile));
            } catch (GeneralSecurityException ex) {
                MidnightCoreAPI.LOGGER.error("Unable to find encryption key for plugin message broker!", ex);
            }
            this.cipher = tempCipher;
        }
    }

    void register(PluginMessenger messenger) {
        this.messengers.add(messenger);
    }

    public void send(String namespace, Message message, boolean encrypt) {

        sendPacket(new MessengerPacket(message.channel, Unpooled.wrappedBuffer(message.payload), encrypt ? cipher : null, namespace, 0, Instant.now(Clock.systemUTC())));
    }

    public void send(String namespace, Message message, boolean encrypt, long ttl) {

        sendPacket(new MessengerPacket(message.channel, Unpooled.wrappedBuffer(message.payload), encrypt ? cipher : null, namespace, ttl, Instant.now(Clock.systemUTC())));
    }

    protected void handle(MessengerPacket packet) {
        for(PluginMessenger messenger : messengers) {
            if(Objects.equals(messenger.namespace, packet.namespace)) {
                messenger.handle(packet);
            }
        }
    }

    protected abstract void sendPacket(MessengerPacket packet);
    protected abstract void shutdown();

    protected static SecretKey readKey(Path file) {
        try(InputStream fis = Files.newInputStream(file)) {

            byte[] buffer = new byte[16];
            if(fis.read(buffer) != 16) {
                MidnightCoreAPI.LOGGER.error("Encryption key was not the right length! Expected a 128-bit key!");
                return null;
            }

            return new SecretKeySpec(buffer, "AES");
        } catch (IOException ex) {
            MidnightCoreAPI.LOGGER.error("Unable to read encryption key!", ex);
            return null;
        }
    }

}
