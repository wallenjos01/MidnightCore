package org.wallentines.mcore.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wallentines.mcore.Skin;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.mdcfg.serializer.ConfigContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.util.UUID;

public class TestSkin {

    @Test
    public void testSkin() {

        UUID skinId = UUID.randomUUID();
        String b64 = "TestValue";
        String sig = "TestSig";
        Skin skin = new Skin(skinId, b64, sig);

        Assertions.assertEquals(skinId, skin.getUUID());
        Assertions.assertEquals(b64, skin.getValue());
        Assertions.assertEquals(sig, skin.getSignature());

    }

    @Test
    public void testSerialization() {

        UUID skinId = UUID.randomUUID();
        String b64 = "TestValue";
        String sig = "TestSig";

        Skin skin = new Skin(skinId, b64, sig);

        ConfigSection serialized = new ConfigSection()
                .with("uid", skinId.toString())
                .with("b64", b64)
                .with("sig", sig);

        SerializeResult<Skin> deserialized = Skin.SERIALIZER.deserialize(ConfigContext.INSTANCE, serialized);

        Assertions.assertTrue(deserialized.isComplete());
        Assertions.assertEquals(skin, deserialized.getOrThrow());

        SerializeResult<ConfigObject> reserialized = Skin.SERIALIZER.serialize(ConfigContext.INSTANCE, skin);

        Assertions.assertTrue(reserialized.isComplete());
        Assertions.assertEquals(serialized, reserialized.getOrThrow());

    }

}
