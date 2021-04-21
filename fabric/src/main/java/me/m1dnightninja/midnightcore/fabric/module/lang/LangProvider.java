package me.m1dnightninja.midnightcore.fabric.module.lang;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangModule;
import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.module.lang.AbstractLangProvider;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.text.ActionBar;
import me.m1dnightninja.midnightcore.fabric.text.Title;
import me.m1dnightninja.midnightcore.fabric.util.ConversionUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class LangProvider extends AbstractLangProvider {

    public LangProvider(File folder, ILangModule mod, ConfigProvider provider, ConfigSection defaultEntries) {
        super(folder, mod, provider, defaultEntries);
    }

    @Override
    public void sendMessage(String key, UUID player, Object... args) {

        ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(player);
        if(pl == null) return;

        MComponent message = getMessage(key, player, args);
        pl.sendMessage(ConversionUtil.toMinecraftComponent(message), ChatType.SYSTEM, Util.NIL_UUID);

    }

    @Override
    public void sendMessage(String key, Iterable<UUID> players, Object... args) {

        HashMap<String, Component> cachedMessages = new HashMap<>();
        for (UUID u : players) {

            ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(u);
            if (pl == null) continue;

            String lang = module.getPlayerLocale(u);
            Component message = cachedMessages.computeIfAbsent(lang, k -> ConversionUtil.toMinecraftComponent(getMessage(key, lang, args)));

            pl.sendMessage(message, ChatType.SYSTEM, Util.NIL_UUID);
        }
    }

    @Override
    public void sendTitle(String key, UUID player, AbstractTitle.TitleOptions opts, Object... args) {

        Title title = new Title(getMessage(key, player, args), opts);
        title.sendToPlayer(player);

    }

    @Override
    public void sendTitle(String key, Iterable<UUID> players, AbstractTitle.TitleOptions opts, Object... args) {

        HashMap<String, Title> cachedMessages = new HashMap<>();
        for(UUID u : players) {

            ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(u);
            if(pl == null) continue;

            String lang = module.getPlayerLocale(u);
            Title title = cachedMessages.computeIfAbsent(lang, k -> new Title(getMessage(key, lang, args), opts));

            title.sendToPlayer(u);
        }
    }

    @Override
    public void sendActionBar(String key, UUID player, AbstractActionBar.ActionBarOptions opts, Object... args) {

        ActionBar title = new ActionBar(getMessage(key, player, args), opts);
        title.sendToPlayer(player);

    }

    @Override
    public void sendActionBar(String key, Iterable<UUID> players, AbstractActionBar.ActionBarOptions opts, Object... args) {

        HashMap<String, ActionBar> cachedMessages = new HashMap<>();
        for(UUID u : players) {

            ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(u);
            if(pl == null) continue;

            String lang = module.getPlayerLocale(u);
            ActionBar title = cachedMessages.computeIfAbsent(lang, k -> new ActionBar(getMessage(key, lang, args), opts));

            title.sendToPlayer(u);

        }
    }
}
