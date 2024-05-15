package org.wallentines.mcore;

import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Singleton;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public abstract class CustomScoreboard {

    protected UnresolvedComponent title;
    protected final UnresolvedComponent[] entries;

    protected NumberFormat numberFormat;
    protected final NumberFormat[] lineFormats;

    protected final Set<WrappedPlayer> viewers = new HashSet<>();

    public CustomScoreboard(UnresolvedComponent title) {
        this.title = title;
        this.entries = new UnresolvedComponent[15];
        this.lineFormats = new NumberFormat[15];
    }

    public void setTitle(UnresolvedComponent title) {
        this.title = title;
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateTitle(pl);
        }
    }


    public void setLine(int line, Component comp) {
        setLine(line, UnresolvedComponent.completed(comp));
    }

    public void setLine(int line, UnresolvedComponent component) {
        if(line < 0 || line > 14) {
            throw new IndexOutOfBoundsException("Line " + line + " is out of the range 0 to 14!");
        }
        if(component == null) {
            entries[line] = null;
        }
        entries[line] = component;
        updateLine(line);
    }

    public void setLine(int line, Component comp, Component numberFormat) {
        setLine(line, UnresolvedComponent.completed(comp), UnresolvedComponent.completed(numberFormat));
    }

    public void setLine(int line, UnresolvedComponent component, UnresolvedComponent numberFormat) {
        if(line < 0 || line > 14) {
            throw new IndexOutOfBoundsException("Line " + line + " is out of the range 0 to 14!");
        }
        entries[line] = component;
        lineFormats[line] = numberFormat == null ? null : new NumberFormat(NumberFormatType.FIXED, numberFormat);

        updateNumberFormat(line);
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


    public void setNumberFormat(NumberFormatType format) {
        setNumberFormat(format, (UnresolvedComponent) null);
    }

    public void setNumberFormat(NumberFormatType format, Component argument) {
        setNumberFormat(format, UnresolvedComponent.completed(argument));
    }

    public void setNumberFormat(NumberFormatType format, UnresolvedComponent argument) {
        numberFormat = new NumberFormat(format, argument);
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateNumberFormat(pl);
        }
    }

    public void setNumberFormat(int line, NumberFormatType format) {
        setNumberFormat(line, format, (UnresolvedComponent) null);
    }

    public void setNumberFormat(int line, NumberFormatType format, Component argument) {
        setNumberFormat(line, format, UnresolvedComponent.completed(argument));
    }

    public void setNumberFormat(int line, NumberFormatType format, UnresolvedComponent argument) {
        lineFormats[line] = new NumberFormat(format, argument);
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateNumberFormat(line, pl);
        }
    }

    protected void updateLine(int i) {
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateLine(i, pl);
        }
    }
    protected void updateNumberFormat(int i) {
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateNumberFormat(i, pl);
        }
    }

    protected abstract void updateTitle(Player player);
    protected abstract void sendToPlayer(Player player);
    protected abstract void clearForPlayer(Player player);
    protected abstract void updateLine(int line, Player player);
    protected abstract void updateNumberFormat(Player player);
    protected abstract void updateNumberFormat(int line, Player player);

    public static CustomScoreboard create(UnresolvedComponent title) {
        return FACTORY.get().create(title);
    }
    public static CustomScoreboard create(Component title) {
        return FACTORY.get().create(UnresolvedComponent.completed(title));
    }

    public static final Singleton<Factory> FACTORY = new Singleton<>();

    public interface Factory {
        CustomScoreboard create(UnresolvedComponent title);
    }

    public enum NumberFormatType {
        DEFAULT,
        BLANK,
        STYLED,
        FIXED
    }

    public static class NumberFormat {
        public final NumberFormatType type;
        public final UnresolvedComponent argument;

        public NumberFormat(NumberFormatType type, UnresolvedComponent argument) {
            this.type = type;
            this.argument = argument;
        }
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
