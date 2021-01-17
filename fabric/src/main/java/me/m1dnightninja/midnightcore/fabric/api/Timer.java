package me.m1dnightninja.midnightcore.fabric.api;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.AbstractTimer;
import me.m1dnightninja.midnightcore.common.FormatUtil;
import me.m1dnightninja.midnightcore.fabric.MidnightCore;
import me.m1dnightninja.midnightcore.fabric.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class Timer extends AbstractTimer {

    private final Component textPrefix;

    public Timer(String prefix, int seconds, boolean countUp, TimerCallback cb) {
        super(prefix, seconds, countUp, cb);
        textPrefix = TextUtil.parse(prefix);
    }

    public Timer(Component prefix, int seconds, boolean countUp, TimerCallback cb) {
        super(prefix.getContents(), seconds, countUp, cb);
        textPrefix = prefix;
    }

    @Override
    protected final void callTick(int secondsLeft) {
        MidnightCore.getServer().submit(() -> {
            try {
                if(callback != null) callback.tick(secondsLeft);
            } catch(Exception ex) {
                MidnightCoreAPI.getLogger().warn("An exception occurred while a timer was ticking!");
                ex.printStackTrace();
            }
        });
    }

    @Override
    protected final void callFinish() {
        MidnightCore.getServer().submit(() -> {
            try {
                if(callback != null) callback.finish();
            } catch(Exception ex) {
                MidnightCoreAPI.getLogger().warn("An exception occurred while a timer was finishing!");
                ex.printStackTrace();
            }
        });
    }

    @Override
    protected final void display() {

        Component send = textPrefix.plainCopy().append(new TextComponent(FormatUtil.formatTime(secondsLeft * 1000L)).setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.WHITE)));

        for(UUID player : players) {
            ServerPlayer pl = MidnightCore.getServer().getPlayerList().getPlayer(player);
            if(pl != null) pl.sendMessage(send, ChatType.GAME_INFO, Util.NIL_UUID);
        }
    }
}
