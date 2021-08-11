package me.m1dnightninja.midnightcore.spigot.module.lang;

import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.lang.ILangProvider;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.lang.AbstractLangModule;
import me.m1dnightninja.midnightcore.common.module.lang.LangProvider;
import me.m1dnightninja.midnightcore.spigot.integration.PlaceholderAPIIntegration;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import me.m1dnightninja.midnightcore.spigot.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class LangModule extends AbstractLangModule {

    private boolean PLACEHOLDERAPI_ENABLED;

    @Override
    public boolean initialize(ConfigSection configuration) {

        super.initialize(configuration);

        PLACEHOLDERAPI_ENABLED = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");

        return true;
    }

    @Override
    public String getPlayerLocale(MPlayer u) {

        if(u == null || ReflectionUtil.MAJOR_VERISON < 12) return getServerLanguage();
        Player p = ((SpigotPlayer) u).getSpigotPlayer();
        if(p == null) return getServerLanguage();

        return p.getLocale();
    }

    @Override
    public String getInlinePlaceholderValue(String key, Object... args) {

        String out = super.getInlinePlaceholderValue(key, args);

        if(out == null && PLACEHOLDERAPI_ENABLED) {

            Player p = null;
            for(Object o : args) {
                if(o instanceof Player) {
                    p = (Player) o;
                    break;
                }
            }

            out = PlaceholderAPIIntegration.getPlaceholderValue(key, p);
        }

        return out;
    }

    @Override
    public ILangProvider createLangProvider(File langFolder, ConfigProvider provider, ConfigSection defaults) {
        return new LangProvider(langFolder, this, provider, defaults);
    }

    public static void sendMessage(CommandSender sender, ILangProvider provider, String id, Object... args) {

        if(sender instanceof Player) {

            provider.sendMessage(id, SpigotPlayer.wrap((Player) sender), args);

        } else {

            String msg = provider.getRawMessage(id, provider.getModule().getServerLanguage());
            msg = provider.getModule().applyPlaceholdersFlattened(msg, args);

            sender.sendMessage(msg);

        }

    }

}
