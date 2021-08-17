package me.m1dnightninja.midnightcore.api.module.lang;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;

public interface ILangProvider {

    MComponent getMessage(String key, String language, Object... args);

    MComponent getUnformattedMessage(String key, String language);

    String getRawMessage(String key, String language);

    MComponent getMessage(String key, MPlayer player, Object... args);

    MComponent getMessage(String key);

    void sendMessage(String key, MPlayer player, Object... args);

    void sendMessage(String key, Iterable<MPlayer> players, Object... args);

    void sendTitle(String key, MPlayer player, MTitle.TitleOptions opts, Object... args);

    void sendTitle(String key, Iterable<MPlayer> players, MTitle.TitleOptions opts, Object... args);

    void sendActionBar(String key, MPlayer player, MActionBar.ActionBarOptions opts, Object... args);

    void sendActionBar(String key, Iterable<MPlayer> players, MActionBar.ActionBarOptions opts, Object... args);

    void reloadAllEntries();

    void saveDefaults(String file);

    void saveEntries(String language);

    void loadEntries(String language, ConfigSection section);


    boolean hasKey(String key);

    boolean hasKey(String key, String language);

    boolean hasKey(String key, MPlayer player);

    ILangModule getModule();

}
