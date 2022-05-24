package org.wallentines.midnightcore.common.text;

import org.wallentines.midnightcore.api.player.MPlayer;
import org.wallentines.midnightcore.api.text.CustomScoreboard;
import org.wallentines.midnightcore.api.text.MComponent;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScoreboard implements CustomScoreboard {

    protected final List<MPlayer> players = new ArrayList<>();
    protected final String id;
    protected MComponent title;

    protected AbstractScoreboard(String id, MComponent title) {

        this.id = id;
        this.title = title;
    }

    @Override
    public void setTitle(MComponent cmp) {
        title = cmp;
    }

    @Override
    public void addViewer(MPlayer player) {

        players.add(player);
        onPlayerAdded(player);
    }

    @Override
    public void removeViewer(MPlayer player) {

        players.remove(player);
        onPlayerRemoved(player);
    }

    @Override
    public void clearViewers() {
        for(int i = 0 ; i < players.size() ; i++) {
            removeViewer(players.get(0));
        }
    }

    protected abstract void onPlayerAdded(MPlayer u);
    protected abstract void onPlayerRemoved(MPlayer u);

}
