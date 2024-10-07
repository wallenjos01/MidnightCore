package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.data.DataManager;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.codec.JSONCodec;

import java.io.File;
import java.nio.file.Paths;

public class TestData {

    @Test
    public void testDataManager() {

        MidnightCoreAPI.FILE_CODEC_REGISTRY.registerFileCodec(JSONCodec.fileCodec());

        File dataFolder = new File("data");
        if(!dataFolder.isDirectory() && !dataFolder.mkdirs()) {
            Assertions.fail("Unable to create data directory!");
        }

        DataManager manager = new DataManager(Paths.get("data"));
        ConfigSection saved = new ConfigSection().with("key", "value").with("number", 42);
        manager.save("test", saved);

        Assertions.assertEquals(saved, manager.getData("test"));
        Assertions.assertEquals("value", manager.getData("test").getString("key"));
        Assertions.assertEquals(42, manager.getData("test").getNumber("number"));

        manager.clearCache();

        Assertions.assertEquals(saved, manager.getData("test"));
        Assertions.assertEquals("value", manager.getData("test").getString("key"));
        Assertions.assertEquals(42, manager.getData("test").getNumber("number"));
    }

}
