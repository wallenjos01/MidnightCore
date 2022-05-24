package org.wallentines.midnightcore.api.module.lang;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightlib.config.ConfigSection;

public interface LangProvider {

    MComponent getMessage(String key, String language, Object... args);

    String getRawMessage(String key, String language);

    boolean hasKey(String key);

    boolean hasKey(String key, String language);

    void saveDefaults(String language);

    void loadEntries(ConfigSection section, String language);

    LangModule getModule();

    MComponent getMessage(String key, MPlayer player, Object... args);

    String getRawMessage(String key, MPlayer player);

    void reload();

}
