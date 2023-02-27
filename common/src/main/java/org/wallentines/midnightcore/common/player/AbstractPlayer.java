package org.wallentines.midnightcore.common.player;

import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.server.MServer;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractPlayer<T> implements MPlayer {

    private final UUID uuid;
    private final MServer server;
    private WeakReference<T> cache = new WeakReference<>(null);

    protected AbstractPlayer(UUID uuid, MServer server) {
        if(uuid == null) throw new IllegalArgumentException("UUID cannot be null!");
        this.uuid = uuid;
        this.server = server;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public MServer getServer() {
        return server;
    }

    @Override
    public boolean isOffline() {
        return cache.get() == null;
    }

    protected void onLogin(T player) {
        cache = new WeakReference<>(player);
    }

    protected abstract T regenCache();

    protected <R> R run(Function<T, R> consumer, Supplier<R> def) {
        T pl = getInternal();
        if(pl == null) return def.get();
        return consumer.apply(pl);
    }

    protected void run(Consumer<T> consumer, Runnable def) {
        T pl = getInternal();
        if(pl == null) {
            def.run();
            return;
        }
        consumer.accept(pl);
    }

    public T getInternal() {
        T out = cache.get();
        if(out == null) {
            out = regenCache();
            onLogin(out);
        }
        return out;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MPlayer && ((MPlayer) obj).getUUID().equals(getUUID());
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public void setSkin(Skin skin) {
        if(getInternal() == null) return;
        SkinModule mod = server.getModule(SkinModule.class);
        if(mod != null) {
            mod.setSkin(this, skin);
            mod.updateSkin(this);
        }
    }

    @Override
    public void resetSkin() {
        if(getInternal() == null) return;
        SkinModule mod = server.getModule(SkinModule.class);
        if(mod != null) {
            mod.resetSkin(this);
            mod.updateSkin(this);
        }
    }

    @Override
    public Skin getSkin() {
        SkinModule mod = server.getModule(SkinModule.class);
        if(mod != null) {
            return mod.getSkin(this);
        }
        return null;
    }
}
