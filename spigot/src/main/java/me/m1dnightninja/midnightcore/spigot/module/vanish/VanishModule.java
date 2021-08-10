package me.m1dnightninja.midnightcore.spigot.module.vanish;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.common.module.vanish.AbstractVanishModule;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.module.savepoint.SavePointCreatedEvent;
import me.m1dnightninja.midnightcore.spigot.module.savepoint.SavePointLoadEvent;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VanishModule extends AbstractVanishModule implements Listener {

    @Override
    public boolean initialize(ConfigSection configuration) {

        Bukkit.getPluginManager().registerEvents(this, MidnightCore.getInstance());

        return true;
    }

    @Override
    protected void doHidePlayer(MPlayer player) {

        for(Player p : Bukkit.getOnlinePlayers()) {

            MPlayer spl = SpigotPlayer.wrap(p);
            if(hiddenFor.containsKey(player) && hiddenFor.get(player).contains(spl)) continue;

            doHidePlayerFor(player, spl);
        }
    }

    @Override
    protected void doHidePlayerFor(MPlayer player, MPlayer other) {

        Player pl = ((SpigotPlayer) player).getSpigotPlayer();
        Player o = ((SpigotPlayer) other).getSpigotPlayer();

        if(pl == null || o == null) return;

        pl.hidePlayer(MidnightCore.getInstance(), o);
    }

    @Override
    protected void doShowPlayer(MPlayer player) {

        for(Player p : Bukkit.getOnlinePlayers()) {

            MPlayer spl = SpigotPlayer.wrap(p);
            if(hiddenFor.containsKey(player) && hiddenFor.get(player).contains(spl)) continue;

            doShowPlayerFor(player, spl);
        }
    }

    @Override
    protected void doShowPlayerFor(MPlayer player, MPlayer other) {

        Player pl = ((SpigotPlayer) player).getSpigotPlayer();
        Player o = ((SpigotPlayer) other).getSpigotPlayer();

        if(pl == null || o == null) return;

        pl.showPlayer(MidnightCore.getInstance(), o);
    }

    @EventHandler
    public void onSavePointCreated(SavePointCreatedEvent event) {

        MPlayer pl = SpigotPlayer.wrap(event.getPlayer());
        saveToConfig(pl, event.getSavePoint().extraData);

    }

    @EventHandler
    public void onSavePointLoad(SavePointLoadEvent event) {

        MPlayer pl = SpigotPlayer.wrap(event.getPlayer());
        loadFromConfig(pl, event.getSavePoint().extraData);

    }

}
