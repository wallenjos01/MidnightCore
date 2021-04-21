package me.m1dnightninja.midnightcore.api.text;

import java.util.UUID;

public abstract class AbstractActionBar {

    protected final MComponent text;
    protected final ActionBarOptions options;

    public AbstractActionBar(MComponent text, ActionBarOptions options) {
        this.text = text;
        this.options = options;
    }

    public MComponent getText() {
        return text;
    }

    public ActionBarOptions getOptions() {
        return options;
    }

    public static class ActionBarOptions {

        public int fadeIn = 20;
        public int stay = 80;
        public int fadeOut = 20;

    }

    public abstract void sendToPlayer(UUID u);
    public abstract void sendToPlayers(Iterable<UUID> u);

}
