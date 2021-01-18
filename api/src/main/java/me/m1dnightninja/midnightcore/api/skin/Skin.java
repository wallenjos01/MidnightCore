package me.m1dnightninja.midnightcore.api.skin;

import java.util.UUID;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.config.ConfigSerializer;

public class Skin {
    public static final ConfigSerializer<Skin> SERIALIZER = new ConfigSerializer<Skin>(){

        @Override
        public Skin deserialize(ConfigSection sec) {
            if (!(sec.has("uid") && sec.has("b64") && sec.has("sig"))) {
                throw new IllegalStateException("Not a skin object!");
            }
            return new Skin(UUID.fromString(sec.getString("uid")), sec.getString("b64"), sec.getString("sig"));
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
}

