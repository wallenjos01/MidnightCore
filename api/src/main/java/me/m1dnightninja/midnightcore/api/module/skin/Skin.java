package me.m1dnightninja.midnightcore.api.module.skin;

import java.util.UUID;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;
import me.m1dnightninja.midnightcore.api.config.InlineSerializer;

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
        return this.uid;
    }

    public String getBase64() {
        return this.b64;
    }

    public String getSignature() {
        return this.sig;
    }

    @Override
    public boolean equals(Object obj) {

        if(!(obj instanceof Skin)) return false;

        Skin s = (Skin) obj;
        return b64.equals(s.b64) && uid.equals(s.uid) && sig.equals(s.sig);

    }

    public static final InlineSerializer<UUID> UID_SERIALIZER = new InlineSerializer<>() {
        @Override
        public UUID deserialize(String s) {
            return UUID.fromString(s);
        }

        @Override
        public String serialize(UUID object) {
            return object.toString();
        }
    };

    public static final ConfigSerializer<Skin> SERIALIZER = new ConfigSerializer<>(){

        @Override
        public Skin deserialize(ConfigSection sec) {
            UUID uid   = sec.has("uid", UUID.class)   ? sec.get("uid", UUID.class) : sec.has("uuid", UUID.class)           ? sec.get("uuid", UUID.class)   : null;
            String b64 = sec.has("b64", String.class) ? sec.getString("b64")       : sec.has("base64", String.class)       ? sec.getString("base64")       : null;
            String sig = sec.has("sig", String.class) ? sec.getString("sig")       : sec.has("signedBase64", String.class) ? sec.getString("signedBase64") : null;

            if (uid == null || b64 == null || sig == null) {
                throw new IllegalStateException("Not a skin object!");
            }
            return new Skin(uid, b64, sig);
        }

        @Override
        public ConfigSection serialize(Skin s) {
            ConfigSection out = new ConfigSection();
            out.set("uid", s.uid.toString());
            out.set("b64", s.b64);
            out.set("sig", s.sig);
            return out;
        }
    };
}

