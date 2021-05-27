package me.m1dnightninja.midnightcore.api.text;


import me.m1dnightninja.midnightcore.api.player.MPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractTimer {

    protected MComponent prefix;
    protected int seconds;
    protected boolean countUp;
    protected List<MPlayer> players;
    private boolean running;
    protected int secondsLeft;
    protected final TimerCallback callback;
    private final TimerTask tick;
    private Timer timer;

    public AbstractTimer(MComponent prefix, final int seconds, final boolean countUp, TimerCallback cb) {
        this.prefix = prefix;
        this.seconds = seconds;
        this.secondsLeft = seconds;
        this.countUp = countUp;
        this.callback = cb;
        this.players = new ArrayList<>();
        this.tick = new TimerTask() {

            @Override
            public void run() {
                display();
                callTick(secondsLeft);
                if (countUp && secondsLeft == seconds || !countUp && secondsLeft == 0) {
                    timer.cancel();
                } else {
                    secondsLeft = countUp ? secondsLeft + 1 : secondsLeft - 1;
                }
            }
        };
    }

    public final void addPlayer(MPlayer player) {
        if (!this.players.contains(player)) {
            this.players.add(player);
        }
    }

    public final void removePlayer(MPlayer player) {
        this.players.remove(player);
    }

    public final void clearPlayers() {
        this.players.clear();
    }

    public final void start() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.timer = new Timer();
        this.timer.schedule(this.tick, 0L, 1000L);
    }

    public final void stop() {
        this.callTick(0);
        this.cancel();
    }

    public final void cancel() {
        if (!this.running) {
            return;
        }
        this.running = false;
        this.timer.cancel();
    }

    public final void reset() {
        if (this.running) {
            this.cancel();
        }
        this.secondsLeft = this.countUp ? 0 : this.seconds;
    }

    protected abstract void callTick(int seconds);

    protected abstract void display();

    public interface TimerCallback {
        void tick(int seconds);
    }
}

