package me.m1dnightninja.midnightcore.spigot.text;

import me.m1dnightninja.midnightcore.api.text.AbstractActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ActionBar extends AbstractActionBar {

    private final BaseComponent spComponent;

    public ActionBar(MComponent text, AbstractActionBar.ActionBarOptions options) {
        super(text, options);

        spComponent = ConversionUtil.toSpigotComponent(text);
    }

    @Override
    public void sendToPlayer(UUID u) {

        Player p = Bukkit.getPlayer(u);
        if(p == null) return;

        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, spComponent);
    }

    @Override
    public void sendToPlayers(Iterable<UUID> u) {

        for(UUID u1 : u) {
            sendToPlayer(u1);
        }
    }
}
