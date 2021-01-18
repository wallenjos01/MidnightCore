package me.m1dnightninja.midnightcore.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public abstract class AbstractTimer {
    protected String prefix;
    protected int seconds;
    protected boolean countUp;
    protected List<UUID> players;
    private boolean running;
    protected int secondsLeft;
    protected final TimerCallback callback;
    private final TimerTask tick;
    private Timer timer;

    public AbstractTimer(String prefix, final int seconds, final boolean countUp, TimerCallback cb) {
        this.prefix = prefix;
        this.seconds = seconds;
        this.secondsLeft = seconds;
        this.countUp = countUp;
        this.callback = cb;
        this.players = new ArrayList<>();
        this.tick = new TimerTask(){

            @Override
            public void run() {
                AbstractTimer.this.display();
                AbstractTimer.this.callTick(AbstractTimer.this.secondsLeft);
                if (countUp && AbstractTimer.this.secondsLeft == seconds || !countUp && AbstractTimer.this.secondsLeft == 0) {
                    AbstractTimer.this.callFinish();
                    AbstractTimer.this.timer.cancel();
                } else {
                    AbstractTimer.this.secondsLeft = countUp ? AbstractTimer.this.secondsLeft + 1 : AbstractTimer.this.secondsLeft - 1;
                }
            }
        };
    }

    public final void addPlayer(UUID player) {
        if (!this.players.contains(player)) {
            this.players.add(player);
        }
    }

    public final void removePlayer(UUID player) {
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
        this.callFinish();
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

    protected abstract void callTick(int var1);

    protected abstract void callFinish();

    protected abstract void display();

    public interface TimerCallback {
        void tick(int var1);

        void finish();
    }
}

