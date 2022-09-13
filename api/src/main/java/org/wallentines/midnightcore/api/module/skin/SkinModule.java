package org.wallentines.midnightcore.api.module.skin;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.midnightlib.module.Module;

import java.util.UUID;
import java.util.function.Consumer;

public interface SkinModule extends Module<MidnightCoreAPI> {

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
