package me.m1dnightninja.midnightcore.api.module.skin;

import me.m1dnightninja.midnightcore.api.module.IModule;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

import java.util.UUID;

public interface ISkinModule
extends IModule {
    Skin getSkin(MPlayer user);

    void getSkinAsync(MPlayer user, SkinCallback cb);

    Skin getOriginalSkin(MPlayer user);

    void getOriginalSkinAsync(MPlayer user, SkinCallback cb);

    Skin getOnlineSkin(UUID user);

    void getOnlineSkinAsync(UUID user, SkinCallback cb);

    void setSkin(MPlayer user, Skin skin);

    void resetSkin(MPlayer user);

    void updateSkin(MPlayer user);
}

