package me.m1dnightninja.midnightcore.spigot.module.lang;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.common.module.lang.AbstractLangProvider;

import java.io.File;
import java.util.UUID;

public class LangProvider extends AbstractLangProvider {

    protected LangProvider(File folder, ILangModule mod, ConfigProvider provider, ConfigSection defaultEntries) {
        super(folder, mod, provider, defaultEntries);
    }

    @Override
    public void sendMessage(String key, UUID player, Object... args) {

    }

    @Override
    public void sendMessage(String key, Iterable<UUID> players, Object... args) {

    }

    @Override
    public void sendTitle(String key, UUID player, AbstractTitle.TitleOptions opts, Object... args) {

    }

    @Override
    public void sendTitle(String key, Iterable<UUID> players, AbstractTitle.TitleOptions opts, Object... args) {

    }

    @Override
    public void sendActionBar(String key, UUID player, AbstractActionBar.ActionBarOptions opts, Object... args) {

    }

    @Override
    public void sendActionBar(String key, Iterable<UUID> players, AbstractActionBar.ActionBarOptions opts, Object... args) {

    }
}
