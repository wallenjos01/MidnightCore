package me.m1dnightninja.midnightcore.api.text;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractCustomScoreboard {

    private final List<UUID> players = new ArrayList<>();
    protected final String id;
    protected MComponent name;

    public AbstractCustomScoreboard(String id, MComponent title) {

        this.id = id;
        this.name = title;
    }

    public void setName(MComponent cmp) {
        name = cmp;
    }

    public abstract void setLine(int line, MComponent message);

    public void addPlayer(UUID player) {

        players.add(player);
    }

    public void removePlayer(UUID player) {

        players.remove(player);
    }

    public void clearPlayers() {
        for(int i = 0 ; i < players.size() ; i++) {
            removePlayer(players.get(0));
        }
    }

    public abstract void update();

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
