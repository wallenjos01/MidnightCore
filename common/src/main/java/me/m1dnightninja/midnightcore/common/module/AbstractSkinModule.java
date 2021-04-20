package me.m1dnightninja.midnightcore.common.module;

import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.module.skin.SkinCallback;
import me.m1dnightninja.midnightcore.common.MojangUtil;

import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractSkinModule implements ISkinModule {


    protected static final MIdentifier ID = MIdentifier.create("midnightcore","skin");

    protected final HashMap<UUID, Skin> loginSkins = new HashMap<>();
    protected final HashMap<UUID, Skin> loadedSkins = new HashMap<>();
    protected final HashMap<UUID, Skin> activeSkins = new HashMap<>();

    @Override
    public MIdentifier getId() {
        return ID;
    }


    @Override
    public Skin getSkin(UUID uid) {

        if(activeSkins.containsKey(uid)) {
            return activeSkins.get(uid);
        }

        return getOnlineSkin(uid);
    }

    @Override
    public void getSkinAsync(UUID uid, SkinCallback callback) {
        Thread t = new Thread(() -> callback.onSkinAvailable(uid, getSkin(uid)));
        t.start();
    }

    @Override
    public Skin getOriginalSkin(UUID uid) {

        if(loginSkins.containsKey(uid)) {
            return loginSkins.get(uid);
        }

        return getOnlineSkin(uid);
    }

    @Override
    public void getOriginalSkinAsync(UUID uid, SkinCallback callback) {
        Thread t = new Thread(() -> callback.onSkinAvailable(uid, getOriginalSkin(uid)));
        t.start();

    }

    @Override
    public Skin getOnlineSkin(UUID uid) {
        return MojangUtil.getSkin(uid);
    }

    @Override
    public void getOnlineSkinAsync(UUID uid, SkinCallback callback) {
        Thread t = new Thread(() -> callback.onSkinAvailable(uid, getOnlineSkin(uid)));
        t.start();
    }

    @Override
    public void setSkin(UUID uid, Skin skin) {
        loadedSkins.put(uid, skin);
    }

    @Override
    public void resetSkin(UUID uid) {
        loadedSkins.put(uid, loginSkins.get(uid));
    }

}
