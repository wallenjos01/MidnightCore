package org.wallentines.midnightcore.spigot.text;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.MComponent;
import org.wallentines.midnightcore.common.text.AbstractScoreboard;

public class SpigotScoreboard extends AbstractScoreboard {


    public SpigotScoreboard(String id, MComponent title) {
        super(id, title);
    }

    @Override
    public void setLine(int line, MComponent message) {

    }

    @Override
    public void update() {

    }

    @Override
    protected void onPlayerAdded(MPlayer u) {

    }

    @Override
    protected void onPlayerRemoved(MPlayer u) {

    }
}
