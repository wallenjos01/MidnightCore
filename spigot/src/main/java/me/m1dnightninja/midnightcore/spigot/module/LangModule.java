package me.m1dnightninja.midnightcore.spigot.module;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.PlaceholderSupplier;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.module.AbstractLangModule;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LangModule extends AbstractLangModule {


    @Override
    public boolean initialize(ConfigSection configuration) {

        registerPlaceholderSupplier("player_name", args -> PlaceholderSupplier.runFor(Player.class, args, pl -> MComponent.createTextComponent(pl.getName())));
        registerPlaceholderSupplier("player_display_name", args -> PlaceholderSupplier.runFor(Player.class, args, pl -> MComponent.Serializer.parseLegacyText(pl.getDisplayName(), '§', null)));

        return true;
    }

    @Override
    public String getPlayerLocale(UUID u) {

        Player p = Bukkit.getPlayer(u);
        if(p == null) return getServerLanguage();

        return p.getLocale();
    }

}
