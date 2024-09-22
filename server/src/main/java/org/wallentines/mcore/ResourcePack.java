package org.wallentines.mcore;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.lang.UnresolvedComponent;

import java.util.UUID;

public class ResourcePack {

    private final UUID uuid;
    private final String url;
    private final String hash;
    private final boolean forced;

    @Nullable
    private final UnresolvedComponent message;

    public ResourcePack(UUID uuid, String url, String hash, boolean forced, @Nullable UnresolvedComponent message) {
        this.uuid = uuid;
        this.url = url;
        this.hash = hash;
        this.forced = forced;
        this.message = message;
    }

    public UUID uuid() {
        return uuid;
    }

    public String url() {
        return url;
    }

    public String hash() {
        return hash;
    }

    public boolean forced() {
        return forced;
    }

    public boolean hasMessage() {
        return message != null;
    }

    @Nullable
    public UnresolvedComponent message() {
        return message;
    }

}
