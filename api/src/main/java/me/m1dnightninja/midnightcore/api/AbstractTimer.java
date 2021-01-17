package me.m1dnightninja.midnightcore.api;

import java.util.*;

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

    public AbstractTimer(String prefix, int seconds, boolean countUp, TimerCallback cb) {
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

                if((countUp && secondsLeft == seconds) || (!countUp && secondsLeft == 0)) {
                    callFinish();
                    timer.cancel();
                } else {

                    secondsLeft = countUp ? secondsLeft + 1 : secondsLeft - 1;
                }
            }
        };
    }

    public final void addPlayer(UUID player) {
        if(!players.contains(player)) players.add(player);
    }

    public final void removePlayer(UUID player) {
        players.remove(player);
    }

    public final void clearPlayers() {
        players.clear();
    }

    public final void start() {
        if(running) return;

        running = true;

        timer = new Timer();
        timer.schedule(tick, 0, 1000);
    }

    public final void stop() {
        callFinish();
        cancel();
    }

    public final void cancel() {
        if(!running) return;

        running = false;
        timer.cancel();
    }

    public final void reset() {

        if(running) cancel();

        secondsLeft = countUp ? 0 : seconds;
    }

    protected abstract void callTick(int secondsLeft);
    protected abstract void callFinish();

    protected abstract void display();

    public interface TimerCallback {
        void tick(int secondsLeft);
        void finish();
    }

}
