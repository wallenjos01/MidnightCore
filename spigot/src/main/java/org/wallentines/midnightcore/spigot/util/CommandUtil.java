package org.wallentines.midnightcore.spigot.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.wallentines.midnightcore.api.text.LangProvider;
import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.spigot.player.SpigotPlayer;

public class CommandUtil {

    public static void sendFeedback(CommandSender sender, LangProvider langProvider, String key, Object... args) {

        if(sender instanceof Player) {
            MPlayer u = SpigotPlayer.wrap((Player) sender);
            MComponent cmp = langProvider.getMessage(key, u, args);

            u.sendMessage(cmp);
            return;
        }

        sender.sendMessage(langProvider.getMessage(key, (String) null, args).getAllContent());

    }

}
