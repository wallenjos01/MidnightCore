package org.wallentines.mcore.skin;

import org.wallentines.mcore.MidnightCoreAPI;
import org.wallentines.mcore.Player;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.Skin;
import org.wallentines.mdcfg.ConfigSection;
import org.wallentines.midnightlib.registry.Identifier;

public abstract class SkinModule implements ServerModule {

    public abstract void setSkin(Player player, Skin skin);

    public abstract void resetSkin(Player player);

    public Skin getSkin(Player player) {
        return player.getSkin();
    }

    public static final Identifier ID = new Identifier(MidnightCoreAPI.MOD_ID, "skin");
    public static final ConfigSection DEFAULT_CONFIG = new ConfigSection().with("get_skins_in_offline_mode", true);

}
