package me.m1dnightninja.midnightcore.spigot.text;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.AbstractTimer;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.MComponent;
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

    private final TextComponent textPrefix;

    public Timer(MComponent prefix, int seconds, boolean countUp, TimerCallback cb) {
        super(prefix, seconds, countUp, cb);
        this.textPrefix = ConversionUtil.toSpigotComponent(prefix);
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

        TextComponent time = new TextComponent(FormatUtil.formatTime(secondsLeft * 1000L));
        time.setBold(true);

        TextComponent comp = textPrefix.duplicate();
        comp.addExtra(time);

        for(MPlayer u : players) {

            Player p = ((SpigotPlayer) u).getSpigotPlayer();

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, comp);
        }
    }
}
