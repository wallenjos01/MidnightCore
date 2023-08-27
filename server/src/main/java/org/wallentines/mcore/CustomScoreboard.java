package org.wallentines.mcore;

import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Singleton;

import java.util.HashSet;
import java.util.Random;
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
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateTitle(pl);
        }
    }


    public void setLine(int line, Component component) {
        if(line < 0 || line > 14) {
            throw new IndexOutOfBoundsException("Line " + line + " is out of the range 0 to 14!");
        }
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
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateLine(i, pl);
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


    protected static String generateRandomId() {

        String values = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rand = new Random();

        StringBuilder builder = new StringBuilder();
        for(int i = 0 ; i < 16 ; i++) {

            int index = rand.nextInt(values.length());
            builder.append(values.charAt(index));
        }

        return builder.toString();
    }

}
