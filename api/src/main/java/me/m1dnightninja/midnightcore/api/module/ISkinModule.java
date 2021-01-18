package me.m1dnightninja.midnightcore.api.module;

import java.util.UUID;
import me.m1dnightninja.midnightcore.api.IModule;
import me.m1dnightninja.midnightcore.api.skin.Skin;
import me.m1dnightninja.midnightcore.api.skin.SkinCallback;

public interface ISkinModule
extends IModule {
    Skin getSkin(UUID var1);

    void getSkinAsync(UUID var1, SkinCallback var2);

    Skin getOriginalSkin(UUID var1);

    void getOriginalSkinAsync(UUID var1, SkinCallback var2);

    Skin getOnlineSkin(UUID var1);

    void getOnlineSkinAsync(UUID var1, SkinCallback var2);

    void setSkin(UUID var1, Skin var2);

    void resetSkin(UUID var1);

    void updateSkin(UUID var1);
}

