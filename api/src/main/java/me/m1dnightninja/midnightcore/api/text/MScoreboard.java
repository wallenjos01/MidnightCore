package me.m1dnightninja.midnightcore.api.text;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.player.MPlayer;

import java.util.ArrayList;
import java.util.List;

public abstract class MScoreboard {

    protected final List<MPlayer> players = new ArrayList<>();
    protected final String id;
    protected MComponent name;

    public MScoreboard(String id, MComponent title) {

        this.id = id;
        this.name = title;
    }

    public void setName(MComponent cmp) {
        name = cmp;
    }

    public abstract void setLine(int line, MComponent message);

    public void addPlayer(MPlayer player) {

        players.add(player);
        onPlayerAdded(player);
    }

    public void removePlayer(MPlayer player) {

        players.remove(player);
        onPlayerRemoved(player);
    }

    public void clearPlayers() {
        for(int i = 0 ; i < players.size() ; i++) {
            removePlayer(players.get(0));
        }
    }

    public abstract void update();

    protected abstract void onPlayerAdded(MPlayer u);
    protected abstract void onPlayerRemoved(MPlayer u);

    private static final String VALUES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateRandomId() {

        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < 16 ; i++) {

            int index = MidnightCoreAPI.getInstance().getRandom().nextInt(VALUES.length());
            builder.append(VALUES.charAt(index));
        }

        return builder.toString();
    }

}
