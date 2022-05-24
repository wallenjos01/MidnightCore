package org.wallentines.midnightcore.api.module.skin;

import org.wallentines.midnightlib.config.serialization.ConfigSerializer;

import java.util.UUID;

public class Skin {

    private final UUID uid;
    private final String b64;
    private final String sig;

    public Skin(UUID uid, String b64, String sig) {
        this.uid = uid;
        this.b64 = b64;
        this.sig = sig;
    }

    public UUID getUUID() {
        return uid;
    }

    public String getValue() {
        return b64;
    }

    public String getSignature() {
        return sig;
    }


    public static final ConfigSerializer<Skin> SERIALIZER = ConfigSerializer.create(
            ConfigSerializer.entry(UUID.class, "uid", Skin::getUUID),
            ConfigSerializer.entry(String.class, "b64", Skin::getValue),
            ConfigSerializer.entry(String.class, "sig", Skin::getSignature),
            Skin::new
    );

}
