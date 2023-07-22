package org.wallentines.mcore.skin;

import org.wallentines.mcore.Player;
import org.wallentines.mcore.ServerModule;
import org.wallentines.mcore.Skin;

public abstract class AbstractSkinModule implements ServerModule {

    public abstract void setSkin(Player player, Skin skin);

    public abstract void resetSkin(Player player);

    public abstract Skin getSkin(Player player);

}
