package org.wallentines.mcore;

import org.wallentines.mcore.lang.PlaceholderContext;
import org.wallentines.mcore.lang.UnresolvedComponent;
import org.wallentines.mcore.text.Component;
import org.wallentines.mdcfg.serializer.*;
import org.wallentines.midnightlib.types.Singleton;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class CustomScoreboard {

    protected UnresolvedComponent title;
    protected final Line[] entries;
    protected NumberFormat numberFormat;
    protected PlaceholderContext context;

    protected final Set<WrappedPlayer> viewers = new HashSet<>();

    public CustomScoreboard(UnresolvedComponent title) {
        this.title = title;
        this.entries = new Line[15];
    }

    public CustomScoreboard(UnresolvedComponent title, PlaceholderContext context) {
        this.title = title;
        this.entries = new Line[15];
        this.context = context;
    }

    public void setTitle(UnresolvedComponent title) {
        this.title = title;
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateTitle(pl);
        }
    }

    public void setContext(PlaceholderContext context) {
        this.context = context;
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
        entries[line] = new Line(component, null);
        updateLine(line);
    }

    public void setLine(int line, Component comp, Component numberFormat) {
        setLine(line, UnresolvedComponent.completed(comp), UnresolvedComponent.completed(numberFormat));
    }

    public void setLine(int line, UnresolvedComponent component, UnresolvedComponent numberFormat) {
        if(line < 0 || line > 14) {
            throw new IndexOutOfBoundsException("Line " + line + " is out of the range 0 to 14!");
        }
        entries[line] = new Line(component, numberFormat == null ? null : new NumberFormat(NumberFormatType.FIXED, numberFormat));

        updateNumberFormat(line);
        updateLine(line);
    }

    public void setLine(int line, UnresolvedComponent component, NumberFormat numberFormat) {
        if(line < 0 || line > 14) {
            throw new IndexOutOfBoundsException("Line " + line + " is out of the range 0 to 14!");
        }
        entries[line] = new Line(component, numberFormat);

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

        if(entries[line] == null) return;
        entries[line] = new Line(entries[line].line.copy(), new NumberFormat(format, argument));
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) updateNumberFormat(line, pl);
        }
    }

    public CustomScoreboard copy() {
        CustomScoreboard out = FACTORY.get().create(title);
        out.context = context;
        out.numberFormat = numberFormat;
        System.arraycopy(entries, 0, out.entries, 0, entries.length);
        return out;
    }

    public void forceUpdate() {
        for(WrappedPlayer p : viewers) {
            Player pl = p.get();
            if(pl != null) {
                updateTitle(pl);
                for(int i = 0 ; i < 15 ; i++) {
                    updateLine(i);
                    updateNumberFormat(i);
                }
            }
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
        DEFAULT("default"),
        BLANK("blank"),
        STYLED("styled"),
        FIXED("fixed");

        private final String id;
        NumberFormatType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static NumberFormatType byId(String id) {
            for(NumberFormatType t : values()) {
                if(t.id.equals(id)) return t;
            }
            return null;
        }

        public static final InlineSerializer<NumberFormatType> SERIALIZER = InlineSerializer.of(NumberFormatType::getId, NumberFormatType::byId);

    }

    public static class NumberFormat {
        public final NumberFormatType type;
        public final UnresolvedComponent argument;

        public NumberFormat(NumberFormatType type, UnresolvedComponent argument) {
            this.type = type;
            this.argument = argument;
        }

        public NumberFormatType type() {
            return type;
        }

        public UnresolvedComponent argument() {
            return argument;
        }

        public static final Serializer<NumberFormat> SERIALIZER = ObjectSerializer.create(
                NumberFormatType.SERIALIZER.entry("type", NumberFormat::type),
                UnresolvedComponent.SERIALIZER.entry("argument", NumberFormat::argument).optional(),
                NumberFormat::new
        );
    }

    public static final Serializer<CustomScoreboard> SERIALIZER = ObjectSerializer.create(
            UnresolvedComponent.SERIALIZER.entry("title", csb -> csb.title),
            NumberFormat.SERIALIZER.<CustomScoreboard>entry("number_format", csb -> csb.numberFormat).optional(),
            Line.SERIALIZER.listOf().entry("lines", csb -> List.of(csb.entries)),
            (title, fmt, lines) -> {
                CustomScoreboard out = FACTORY.get().create(title);
                out.numberFormat = fmt;
                int i = 0;
                for(Line l : lines) {
                    out.entries[i] = l;
                }
                return out;
            }
    );

    protected static class Line {

        private final UnresolvedComponent line;
        private final NumberFormat format;

        private Line(UnresolvedComponent line, NumberFormat format) {
            this.line = line;
            this.format = format;
        }

        public UnresolvedComponent line() {
            return line;
        }

        public NumberFormat format() {
            return format;
        }

        private static final Serializer<Line> SERIALIZER = ObjectSerializer.create(
                UnresolvedComponent.SERIALIZER.entry("line", Line::line),
                NumberFormat.SERIALIZER.entry("format", Line::format).optional(),
                Line::new
        ).or(new InlineSerializer<Line>() {
            @Override
            public SerializeResult<Line> readString(String str) {
                return UnresolvedComponent.SERIALIZER.readString(str).flatMap(uc -> new Line(uc, null));
            }

            @Override
            public SerializeResult<String> writeString(Line value) {
                return UnresolvedComponent.SERIALIZER.writeString(value.line);
            }
        });
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
