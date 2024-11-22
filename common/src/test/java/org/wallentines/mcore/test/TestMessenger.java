package org.wallentines.mcore.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.messenger.MessengerPacket;
import org.wallentines.mcore.messenger.PluginMessageBroker;
import org.wallentines.smi.Message;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TestMessenger {


    private static class DummyPluginMessageBroker extends PluginMessageBroker {

        public DummyPluginMessageBroker(boolean encrypt) {
            super(encrypt ? Paths.get("messenger.key") : null);
        }

        @Override
        protected void sendPacket(MessengerPacket packet) { }

        @Override
        protected void shutdown() { }


        public void testPackets() {

            long value = 123810945L;

            ByteBuf data = Unpooled.buffer();
            data.writeLong(value);

            String packetId = "test";

            MessengerPacket packet = new MessengerPacket(packetId, data, cipher, null, 1000);
            Assertions.assertEquals(cipher != null, packet.getFlags().contains(MessengerPacket.Flag.ENCRYPTED));

            ByteBuf encoded = Unpooled.buffer();
            packet.write(encoded);

            MessengerPacket decoded = MessengerPacket.readPacket(encoded, cipher);

            Assertions.assertEquals(cipher != null, decoded.getFlags().contains(MessengerPacket.Flag.ENCRYPTED));

            Message msg = decoded.toMessage();

            Assertions.assertEquals(packetId, msg.channel);
            Assertions.assertEquals(value, msg.payload.getLong());

        }
    }

    @Test
    public void testUnencryptedPackets() {

        new DummyPluginMessageBroker(false).testPackets();

    }

    @Test
    public void testEncryptedPackets() {

        SecureRandom random = new SecureRandom();
        File f = new File("messenger.key");
        try(FileOutputStream fos = new FileOutputStream(f)) {

            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128, random);
            SecretKey key = keyGenerator.generateKey();
            fos.write(key.getEncoded());

        } catch (IOException | NoSuchAlgorithmException ex) {
            Assertions.fail("Unable to write keyfile!");
        }

        new DummyPluginMessageBroker(true).testPackets();

    }

}
