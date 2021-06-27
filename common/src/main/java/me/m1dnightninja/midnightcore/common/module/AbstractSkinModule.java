package me.m1dnightninja.midnightcore.common.module;

import com.mojang.authlib.GameProfile;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.module.skin.ISkinModule;
import me.m1dnightninja.midnightcore.api.module.skin.Skin;
import me.m1dnightninja.midnightcore.api.module.skin.SkinCallback;
import me.m1dnightninja.midnightcore.common.util.MojangUtil;

import java.util.HashMap;
import java.util.UUID;

public abstract class AbstractSkinModule implements ISkinModule {


    protected static final MIdentifier ID = MIdentifier.create("midnightcore","skin");

    protected final HashMap<MPlayer, Skin> loginSkins = new HashMap<>();
    protected final HashMap<MPlayer, Skin> loadedSkins = new HashMap<>();
    protected final HashMap<MPlayer, Skin> activeSkins = new HashMap<>();

    protected boolean getOfflineModeSkins = true;

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public boolean initialize(ConfigSection configuration) {

        getOfflineModeSkins = configuration.getBoolean("get_skins_in_offline_mode");
        return true;
    }

    @Override
    public ConfigSection getDefaultConfig() {

        ConfigSection out = new ConfigSection();
        out.set("get_skins_in_offline_mode", true);

        return out;
    }

    @Override
    public Skin getSkin(MPlayer uid) {

        if(activeSkins.containsKey(uid)) {
            return activeSkins.get(uid);
        }

        return getOnlineSkin(uid.getUUID());
    }

    @Override
    public void getSkinAsync(MPlayer uid, SkinCallback callback) {
        Thread t = new Thread(() -> callback.onSkinAvailable(uid.getUUID(), getSkin(uid)));
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
    public void getOriginalSkinAsync(MPlayer uid, SkinCallback callback) {
        Thread t = new Thread(() -> callback.onSkinAvailable(uid.getUUID(), getOriginalSkin(uid)));
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

}
