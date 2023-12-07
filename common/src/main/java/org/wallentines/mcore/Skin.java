package org.wallentines.mcore;

import org.wallentines.mdcfg.serializer.ObjectSerializer;
import org.wallentines.mdcfg.serializer.Serializer;

import java.util.Objects;
import java.util.UUID;

/**
 * A data type to store player skin information
 */
public class Skin {

    private final UUID uid;
    private final String b64;
    private final String sig;

    /**
     * Constructs a new skin object
     * @param uid The UUID of the player who uploaded the skin
     * @param b64 The value of the skin
     * @param sig The signature of the skin
     */
    public Skin(UUID uid, String b64, String sig) {
        this.uid = uid;
        this.b64 = b64;
        this.sig = sig;
    }


    /**
     * The UUID of the player who first uploaded and applied the skin
     * @return The UUID of the player who uploaded the skin
     */
    public UUID getUUID() {
        return uid;
    }

    /**
     * Returns the skin's value. Including the model type and texture URL, written in JSON and encoded in Base64.
     * @return The skin's value
     */
    public String getValue() {
        return b64;
    }

    /**
     * Returns the skin's signature. Signed with Mojang's private key, clients will not display a skin unless it has a
     * valid signature corresponding to the skin's value
     * @return The skin's signature
     */
    public String getSignature() {
        return sig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skin skin = (Skin) o;
        return Objects.equals(uid, skin.uid) && Objects.equals(b64, skin.b64) && Objects.equals(sig, skin.sig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, b64, sig);
    }

    /**
     * A serializer for saving and loading Skin objects from disk
     */
    public static final Serializer<Skin> SERIALIZER = ObjectSerializer.create(
            Serializer.UUID.entry("uid", Skin::getUUID),
            Serializer.STRING.entry("b64", Skin::getValue),
            Serializer.STRING.entry("sig", Skin::getSignature),
            Skin::new
    );
}
