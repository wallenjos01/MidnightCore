package me.m1dnightninja.midnightcore.spigot.text;

import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MTimer;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.text.MActionBar;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.api.text.MStyle;
import me.m1dnightninja.midnightcore.common.util.FormatUtil;
import me.m1dnightninja.midnightcore.spigot.MidnightCore;
import org.bukkit.scheduler.BukkitRunnable;

public class SpigotTimer extends MTimer {

    public SpigotTimer(MComponent prefix, int seconds, boolean countUp, TimerCallback cb) {
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

            u.sendActionBar(new MActionBar(comp, new MActionBar.ActionBarOptions()));
        }
    }
}
