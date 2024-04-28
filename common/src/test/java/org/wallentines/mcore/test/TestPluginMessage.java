package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.messenger.PluginMessageBroker;

import java.util.EnumSet;

public class TestPluginMessage {

    @Test
    public void testFlags() {

        int max = (int) Math.pow(2, PluginMessageBroker.Flag.values().length);

        for(byte i = 0 ; i < max ; i++) {

            EnumSet<PluginMessageBroker.Flag> flags = PluginMessageBroker.Flag.unpack(i);
            Assertions.assertEquals(i, PluginMessageBroker.Flag.pack(flags));

        }

    }

}
