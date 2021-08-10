package me.m1dnightninja.midnightcore.velocity.text;

import com.velocitypowered.api.proxy.Player;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.text.MTimer;
import me.m1dnightninja.midnightcore.api.text.MComponent;
import me.m1dnightninja.midnightcore.common.util.FormatUtil;
import me.m1dnightninja.midnightcore.velocity.MidnightCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class VelocityTimer extends MTimer {

    private final Component prefix;

    public VelocityTimer(MComponent prefix, int seconds, boolean countUp, TimerCallback cb) {
        super(prefix, seconds, countUp, cb);

        this.prefix = GsonComponentSerializer.gson().deserialize(MComponent.Serializer.toJsonString(prefix));
    }

    @Override
    protected void callTick(int seconds) {
        MidnightCore.getInstance().getServer().getScheduler().buildTask(MidnightCore.getInstance(), () -> callback.tick(seconds)).delay(0, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void display() {

        ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();
        prefix.componentBuilderApply(builder);

        String time = FormatUtil.formatTime(secondsLeft * 1000L);
        Component timer = Component.text().content(time).style(Style.style(TextDecoration.BOLD)).build();
        builder.append(timer);

        Component send = builder.build();

        for(MPlayer u : players) {

            Optional<Player> optionalPlayer = MidnightCore.getInstance().getServer().getPlayer(u.getUUID());
            optionalPlayer.ifPresent(player -> player.sendActionBar(send));

        }

    }
}
