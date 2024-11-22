package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.messenger.MessengerPacket;

import java.util.EnumSet;

public class TestPluginMessage {

    @Test
    public void testFlags() {

        int max = (int) Math.pow(2, MessengerPacket.Flag.values().length);

        for(byte i = 0 ; i < max ; i++) {

            EnumSet<MessengerPacket.Flag> flags = MessengerPacket.Flag.unpack(i);
            Assertions.assertEquals(i, MessengerPacket.Flag.pack(flags));

        }

    }

}
