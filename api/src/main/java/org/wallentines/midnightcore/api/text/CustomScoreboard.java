package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;

import java.util.Random;

@SuppressWarnings("unused")
public interface CustomScoreboard {

    void setTitle(MComponent title);

    void setLine(int line, MComponent message);

    void addViewer(MPlayer player);

    void removeViewer(MPlayer player);

    void clearViewers();

    void update();

    static String generateRandomId() {

        String values = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        MidnightCoreAPI api = MidnightCoreAPI.getInstance();
        Random rand = api == null ? new Random() : api.getRandom();

        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < 16 ; i++) {

            int index = rand.nextInt(values.length());
            builder.append(values.charAt(index));
        }

        return builder.toString();
    }

    interface Factory {
        CustomScoreboard create(String id, MComponent title);
    }

}
