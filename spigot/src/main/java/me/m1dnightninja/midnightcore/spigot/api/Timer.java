package me.m1dnightninja.midnightcore.spigot.api;

import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.common.FormatUtil;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Timer extends AbstractTimer {

    private final TextComponent textPrefix;

    public Timer(TextComponent prefix, int seconds, boolean countUp, TimerCallback cb) {
        super(prefix.toPlainText(), seconds, countUp, cb);
        this.textPrefix = prefix;
    }

    public Timer(String prefix, int seconds, boolean countUp, TimerCallback cb) {
        super(prefix, seconds, countUp, cb);
        textPrefix = new TextComponent(TextComponent.fromLegacyText(prefix));
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

        for(UUID u : players) {

            Player p = Bukkit.getPlayer(u);
            if(p == null) continue;

            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, comp);
        }
    }
}
