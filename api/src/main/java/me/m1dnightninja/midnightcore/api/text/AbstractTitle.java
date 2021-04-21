package me.m1dnightninja.midnightcore.api.text;

import java.util.UUID;

public abstract class AbstractTitle {

    protected final MComponent text;
    protected final TitleOptions options;

    public AbstractTitle(MComponent text, TitleOptions options) {
        this.text = text;
        this.options = options;
    }

    public MComponent getText() {
        return text;
    }

    public TitleOptions getOptions() {
        return options;
    }

    public static class TitleOptions {

        public boolean clear = false;
        public boolean subtitle = false;
        public int fadeIn = 20;
        public int stay = 80;
        public int fadeOut = 20;

        static TitleOptions create(boolean clear, boolean subtitle, int fadeIn, int stay, int fadeOut) {
            TitleOptions out = new TitleOptions();
            out.clear = clear;
            out.subtitle = subtitle;
            out.fadeIn = fadeIn;
            out.stay = stay;
            out.fadeOut = fadeOut;
            return out;
        }

    }

    public abstract void sendToPlayer(UUID u);
    public abstract void sendToPlayers(Iterable<UUID> u);

    public static final TitleOptions TITLE  = TitleOptions.create(false, false, 20, 80, 20);
    public static final TitleOptions SUBTITLE  = TitleOptions.create(false, true, 20, 80, 20);
    public static final TitleOptions TITLE_CLEAR  = TitleOptions.create(true, false, 20, 80, 20);
    public static final TitleOptions SUBTITLE_CLEAR  = TitleOptions.create(true, true, 20, 80, 20);

}
