package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightcore.api.player.MPlayer;

public interface CustomScoreboard {

    void setTitle(MComponent component);

    void setLine(int line, MComponent message);

    void addViewer(MPlayer player);

    void removeViewer(MPlayer player);

    void clearViewers();

    void update();

    static String generateRandomId() {

        String values = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < 16 ; i++) {

            int index = MidnightCoreAPI.getInstance().getRandom().nextInt(values.length());
            builder.append(values.charAt(index));
        }

        return builder.toString();
    }

}
