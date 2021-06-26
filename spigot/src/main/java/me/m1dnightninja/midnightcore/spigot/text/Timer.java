package me.m1dnightninja.midnightcore.spigot.text;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.AbstractTimer;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.ActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;
import me.m1dnightninja.midnightcore.common.util.FormatUtil;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import me.m1dnightninja.midnightcore.spigot.player.SpigotPlayer;
import me.m1dnightninja.midnightcore.spigot.util.ConversionUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Timer extends AbstractTimer {

    public Timer(MComponent prefix, int seconds, boolean countUp, TimerCallback cb) {
        super(prefix, seconds, countUp, cb);
    }


    @Override
    protected void callTick(int secondsLeft) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if(callback != null) callback.tick(secondsLeft);
                } catch(Exception ex) {
                    MidnightCoreAPI.getLogger().warn("An exception occurred while a timer was ticking!");
                    ex.printStackTrace();
                }
            }
        }.runTask(MidnightCore.getPlugin(MidnightCore.class));

    }

    @Override
    protected void display() {

        MComponent time = MComponent.createTextComponent(FormatUtil.formatTime(secondsLeft * 1000L));
        time.setStyle(new MStyle().withBold(true));

        MComponent comp = prefix.copy();
        comp.addChild(time);

        for(MPlayer u : players) {

            u.sendActionBar(new ActionBar(comp, new ActionBar.ActionBarOptions()));
        }
    }
}
