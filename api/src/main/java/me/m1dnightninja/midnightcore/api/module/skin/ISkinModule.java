package me.m1dnightninja.midnightcore.api.module.skin;

import java.util.UUID;

import me.m1dnightninja.midnightcore.api.module.IModule;

public interface ISkinModule
extends IModule {
    Skin getSkin(UUID user);

    void getSkinAsync(UUID user, SkinCallback cb);

    Skin getOriginalSkin(UUID user);

    void getOriginalSkinAsync(UUID user, SkinCallback cb);

    Skin getOnlineSkin(UUID user);

    void getOnlineSkinAsync(UUID user, SkinCallback cb);

    void setSkin(UUID user, Skin skin);

    void resetSkin(UUID user);

    void updateSkin(UUID user);
}

