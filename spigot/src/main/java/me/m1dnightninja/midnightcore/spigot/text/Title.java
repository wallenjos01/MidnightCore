package me.m1dnightninja.midnightcore.spigot.text;

import me.m1dnightninja.midnightcore.api.text.AbstractTitle;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Title extends AbstractTitle {

    private final String legacy;

    public Title(MComponent text, TitleOptions options) {
        super(text, options);
        legacy = text.toLegacyText(false);
    }

    @Override
    public void sendToPlayer(UUID u) {

        Player p = Bukkit.getPlayer(u);
        if(p == null) return;

        p.sendTitle(options.subtitle ? "" : legacy, options.subtitle ? legacy : "", options.fadeIn, options.stay, options.fadeOut);
    }

    @Override
    public void sendToPlayers(Iterable<UUID> u) {

        for(UUID u1 : u) {
            sendToPlayer(u1);
        }
    }
}
