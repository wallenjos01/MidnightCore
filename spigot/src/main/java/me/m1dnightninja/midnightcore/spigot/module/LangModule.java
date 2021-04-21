package me.m1dnightninja.midnightcore.spigot.module;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.module.lang.PlaceholderSupplier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.module.lang.AbstractLangModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class LangModule extends AbstractLangModule {


    @Override
    public boolean initialize(ConfigSection configuration) {

        registerPlaceholderSupplier("player_name", PlaceholderSupplier.create(Player.class, pl -> MComponent.createTextComponent(pl.getName())));
        registerPlaceholderSupplier("player_display_name", PlaceholderSupplier.create(Player.class, pl -> MComponent.Serializer.parseLegacyText(pl.getDisplayName(), '§', null)));

        return true;
    }

    @Override
    public String getPlayerLocale(UUID u) {

        Player p = Bukkit.getPlayer(u);
        if(p == null) return getServerLanguage();

        return p.getLocale();
    }

    @Override
    public ILangProvider createLangProvider(File langFolder, ConfigProvider provider, ConfigSection defaults) {
        return new LangProvider(langFolder, this, provider, defaults);
    }

}
