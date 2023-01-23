package org.wallentines.midnightcore.api.module.skin;

import org.wallentines.midnightcore.api.module.ServerModule;
import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface SkinModule extends ServerModule {

    Skin getSkin(MPlayer user);

    void getSkinAsync(MPlayer user, Consumer<Skin> cb);

    Skin getOriginalSkin(MPlayer user);

    void getOriginalSkinAsync(MPlayer user, Consumer<Skin> cb);

    Skin getOnlineSkin(UUID user);

    void getOnlineSkinAsync(UUID user, Consumer<Skin> cb);

    void setSkin(MPlayer user, Skin skin);

    void resetSkin(MPlayer user);

    void updateSkin(MPlayer user);

}
