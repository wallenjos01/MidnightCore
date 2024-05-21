package org.wallentines.mcore.test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.messenger.Message;
import org.wallentines.mcore.messenger.PluginMessageBroker;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class TestMessenger {


    private static class DummyPluginMessageBroker extends PluginMessageBroker {

        public DummyPluginMessageBroker(boolean encrypt) {
            init(encrypt);
        }

        @Override
        protected void send(Packet packet) { }

        @Override
        protected void onShutdown() { }

        @Override
        protected File getKeyFile() {
            return new File("messenger.key");
        }

        public void testPackets() {

            long value = 123810945L;

            ByteBuf data = Unpooled.buffer();
            data.writeLong(value);

            Packet packet = new Packet("test", shouldEncrypt(), null, 1000, data);

            Assertions.assertEquals(shouldEncrypt(), packet.getFlags().contains(Flag.ENCRYPTED));

            ByteBuf encoded = Unpooled.buffer();
            packet.write(encoded);

            Packet decoded = readPacket(encoded);

            Assertions.assertEquals(shouldEncrypt(), decoded.getFlags().contains(Flag.ENCRYPTED));

            Message msg = decoded.toMessage(null);

            Assertions.assertEquals("test", msg.channel());
            Assertions.assertEquals(value, msg.payload().readLong());

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
