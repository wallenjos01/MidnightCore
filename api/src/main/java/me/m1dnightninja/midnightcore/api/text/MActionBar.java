package me.m1dnightninja.midnightcore.api.text;

public class MActionBar {

    protected final MComponent text;
    protected final ActionBarOptions options;

    public MActionBar(MComponent text, ActionBarOptions options) {
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



}
