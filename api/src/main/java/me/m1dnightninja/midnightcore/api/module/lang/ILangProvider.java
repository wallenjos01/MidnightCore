package me.m1dnightninja.midnightcore.api.module.lang;

import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;

import java.util.UUID;

public interface ILangProvider {

    MComponent getMessage(String key, String language, Object... args);

    MComponent getUnformattedMessage(String key, String language);

    String getRawMessage(String key, String language);

    MComponent getMessage(String key, UUID player, Object... args);

    MComponent getMessage(String key);

    void sendMessage(String key, UUID player, Object... args);

    void sendMessage(String key, Iterable<UUID> players, Object... args);

    void sendTitle(String key, UUID player, AbstractTitle.TitleOptions opts, Object... args);

    void sendTitle(String key, Iterable<UUID> players, AbstractTitle.TitleOptions opts, Object... args);

    void sendActionBar(String key, UUID player, AbstractActionBar.ActionBarOptions opts, Object... args);

    void sendActionBar(String key, Iterable<UUID> players, AbstractActionBar.ActionBarOptions opts, Object... args);

    void reloadAllEntries();

    void saveDefaults(String file);

    boolean hasKey(String key);

    boolean hasKey(String key, String language);

    boolean hasKey(String key, UUID player);

}
