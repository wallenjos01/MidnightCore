package org.wallentines.midnightcore.api.module.data;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightlib.config.ConfigSection;

@SuppressWarnings("unused")
@Deprecated
public interface DataProvider {

    ConfigSection getData(String id);

    void setData(String id, ConfigSection sec);

    void saveData(String id);

    void clearData(String id);

    ConfigSection reloadData(String id);

    void saveAll();

    default ConfigSection getData(MPlayer player) { return getData(player.getUUID().toString()); }

    default void setData(MPlayer player, ConfigSection section) { setData(player.getUUID().toString(), section); }

    default void saveData(MPlayer player) { saveData(player.getUUID().toString()); }

    default void clearData(MPlayer player) { clearData(player.getUUID().toString()); }

    default ConfigSection reloadData(MPlayer player) { return reloadData(player.getUUID().toString()); }

}
