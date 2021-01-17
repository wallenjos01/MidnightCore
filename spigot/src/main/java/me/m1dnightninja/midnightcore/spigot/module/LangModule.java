package me.m1dnightninja.midnightcore.spigot.module;

import me.m1dnightninja.midnightcore.api.lang.AbstractLangProvider;
import me.m1dnightninja.midnightcore.common.module.AbstractLangModule;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class LangModule extends AbstractLangModule<BaseComponent> {

    @Override
    public String getLanguage(UUID player) {

        Player p = Bukkit.getPlayer(player);
        if(p == null) return null;

        return p.getLocale();
    }

    @Override
    public String getServerLanguage() {
        return "en_us";
    }

    @Override
    public AbstractLangProvider createProvider(String name, File folder) {
        return null;
    }

    @Override
    public AbstractLangProvider getProvider(String name) {
        return null;
    }


    @Override
    public boolean initialize() {
        return true;
    }
}
