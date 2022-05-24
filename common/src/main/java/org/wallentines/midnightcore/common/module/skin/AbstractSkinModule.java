package org.wallentines.midnightcore.common.module.skin;

import com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.module.savepoint.SavepointCreatedEvent;
import org.wallentines.midnightcore.api.module.savepoint.SavepointLoadedEvent;
import org.wallentines.midnightcore.api.module.skin.Skin;
import org.wallentines.midnightcore.api.module.skin.SkinModule;
import org.wallentines.midnightcore.api.module.skin.SkinUpdateEvent;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.common.Constants;
import org.wallentines.midnightcore.common.player.AbstractPlayer;
import org.wallentines.midnightcore.common.util.MojangUtil;
import org.wallentines.midnightlib.config.ConfigSection;
import org.wallentines.midnightlib.event.Event;
import org.wallentines.midnightlib.registry.Identifier;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractSkinModule implements SkinModule {

    protected static final Logger LOGGER = LogManager.getLogger("SkinModule");

    private final HashMap<MPlayer, Skin> loginSkins = new HashMap<>();
    private final HashMap<MPlayer, Skin> loadedSkins = new HashMap<>();
    private final HashMap<MPlayer, Skin> activeSkins = new HashMap<>();

    protected boolean getOfflineModeSkins = true;

    @Override
    public boolean initialize(ConfigSection configuration, MidnightCoreAPI api) {

        getOfflineModeSkins = configuration.getBoolean("get_skins_in_offline_mode");

        Event.register(SavepointCreatedEvent.class, this, event -> event.getSavepoint().getExtraData().set("skin", getSkin(event.getPlayer())));
        Event.register(SavepointLoadedEvent.class, this, 100, event -> {

            if(event.isCancelled() || !event.getSavepoint().getExtraData().has("skin")) return;

            MPlayer player = event.getPlayer();

            setSkin(player, event.getSavepoint().getExtraData().get("skin", Skin.class));
            updateSkin(player);
        });

        return true;
    }

    @Override
    public Skin getSkin(MPlayer uid) {

        if(activeSkins.containsKey(uid)) {
            return activeSkins.get(uid);
        }

        return getOnlineSkin(uid.getUUID());
    }

    @Override
    public void getSkinAsync(MPlayer uid, Consumer<Skin> callback) {
        Thread t = new Thread(() -> callback.accept(getSkin(uid)));
        t.start();
    }

    @Override
    public Skin getOriginalSkin(MPlayer uid) {

        if(loginSkins.containsKey(uid)) {
            return loginSkins.get(uid);
        }

        return getOnlineSkin(uid.getUUID());
    }

    @Override
    public void getOriginalSkinAsync(MPlayer uid, Consumer<Skin> callback) {
        Thread t = new Thread(() -> callback.accept(getOriginalSkin(uid)));
        t.start();

    }

    @Override
    public Skin getOnlineSkin(UUID uid) {
        return MojangUtil.getSkin(uid);
    }

    @Override
    public void getOnlineSkinAsync(UUID uid, Consumer<Skin> callback) {
        Thread t = new Thread(() -> callback.accept(getOnlineSkin(uid)));
        t.start();
    }

    @Override
    public void setSkin(MPlayer uid, Skin skin) {
        loadedSkins.put(uid, skin);
    }

    @Override
    public void resetSkin(MPlayer uid) {
        loadedSkins.put(uid, loginSkins.get(uid));
    }

    protected void findOfflineSkin(MPlayer player, GameProfile prof) {

        String name = prof.getName();

        new Thread(() -> {

            UUID onlineUid = MojangUtil.getUUID(name);
            if(onlineUid == null) return;

            Skin s = MojangUtil.getSkin(onlineUid);

            loginSkins.put(player, s);

            if(!activeSkins.containsKey(player)) {

                setSkin(player, s);
                updateSkin(player);
            }

        }).start();
    }

    @Override
    public void updateSkin(MPlayer user) {

        if(!loadedSkins.containsKey(user) && !activeSkins.containsKey(user)) return;

        Skin oldSkin = activeSkins.get(user);
        Skin newSkin = loadedSkins.get(user);

        SkinUpdateEvent ev = new SkinUpdateEvent((AbstractPlayer<?>) user, oldSkin, newSkin);
        Event.invoke(ev);

        if(!ev.isCancelled()) {

            activeSkins.put(user, ev.getNewSkin());
            doUpdate(user, ev.getNewSkin());
        }
    }

    protected void setLoginSkin(MPlayer user, Skin skin) {
        loginSkins.put(user, skin);
    }

    protected void onLeave(MPlayer user) {

        loginSkins.remove(user);
        activeSkins.remove(user);
        loadedSkins.remove(user);
    }

    protected void setActiveSkin(MPlayer mpl, Skin skin) {
        activeSkins.put(mpl, skin);
    }

    protected Skin getActiveSkin(MPlayer mpl) {
        return activeSkins.get(mpl);
    }

    protected abstract void doUpdate(MPlayer mpl, Skin skin);

    protected static final ConfigSection DEFAULT_CONFIG = new ConfigSection().with("get_skins_in_offline_mode", true);
    public static final Identifier ID = new Identifier(Constants.DEFAULT_NAMESPACE, "skin");

}
