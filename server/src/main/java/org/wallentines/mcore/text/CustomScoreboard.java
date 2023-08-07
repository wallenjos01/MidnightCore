package org.wallentines.mcore.text;

import org.wallentines.mcore.Player;
import org.wallentines.mcore.WrappedPlayer;
import org.wallentines.midnightlib.types.Singleton;

import java.util.HashSet;
import java.util.Set;

public abstract class CustomScoreboard {

    protected Component title;
    protected final Component[] entries;

    protected final Set<WrappedPlayer> viewers = new HashSet<>();

    public CustomScoreboard(Component title) {
        this.title = title;
        this.entries = new Component[15];
    }

    public void setTitle(Component title) {
        this.title = title;
    }


    public void setLine(int line, Component component) {
        if(component == null) {
            entries[line] = null;
        }
        entries[line] = component;
        updateLine(line);
    }

    public void addViewer(Player player) {
        viewers.add(player.wrap());
        sendToPlayer(player);
    }

    public void removeViewer(Player player) {
        viewers.remove(player.wrap());
        clearForPlayer(player);
    }

    protected void updateLine(int i) {
        for(WrappedPlayer pl : viewers) {
            updateLine(i, pl.get());
        }
    }

    protected abstract void updateTitle(Player player);
    protected abstract void sendToPlayer(Player player);
    protected abstract void clearForPlayer(Player player);
    protected abstract void updateLine(int line, Player player);


    public static final Singleton<Factory> FACTORY = new Singleton<>();

    public interface Factory {
        CustomScoreboard create(Component title);
    }

}