package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightlib.registry.StringRegistry;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class PlaceholderManager {

    private final StringRegistry<PlaceholderSupplier<MComponent>> placeholders = new StringRegistry<>();
    private final StringRegistry<PlaceholderSupplier<String>> inlinePlaceholders = new StringRegistry<>();

    public StringRegistry<PlaceholderSupplier<MComponent>> getPlaceholders() {
        return placeholders;
    }

    public StringRegistry<PlaceholderSupplier<String>> getInlinePlaceholders() {
        return inlinePlaceholders;
    }

    public PlaceholderSupplier<MComponent> getPlaceholderSupplier(String key) {

        return placeholders.get(key);
    }

    public PlaceholderSupplier<String> getInlinePlaceholderSupplier(String key) {

        return inlinePlaceholders.get(key);
    }

    public String getInlinePlaceholderValue(PlaceholderSupplier.PlaceholderContext ctx) {

        CustomPlaceholderInline cp = find(CustomPlaceholderInline.class, pl -> pl.getId().equals(ctx.getName()), ctx.getArgs());
        if(cp != null) return cp.get();

        return PlaceholderSupplier.get(inlinePlaceholders.get(ctx.getName()), ctx);
    }

    public MComponent getPlaceholderValue(PlaceholderSupplier.PlaceholderContext ctx) {

        CustomPlaceholder cp = find(CustomPlaceholder.class, pl -> pl.getId().equals(ctx.getName()), ctx.getArgs());
        if(cp != null) return cp.get();

        return PlaceholderSupplier.get(placeholders.get(ctx.getName()), ctx);
    }

    public String getFlattenedPlaceholderValue(PlaceholderSupplier.PlaceholderContext ctx) {

        CustomPlaceholder cp = find(CustomPlaceholder.class, pl -> pl.getId().equals(ctx.getName()), ctx.getArgs());
        if(cp != null) return cp.get().getAllContent();

        return Optional.ofNullable(PlaceholderSupplier.get(placeholders.get(ctx.getName()), ctx)).map(MComponent::getAllContent).orElse(null);
    }

    public String applyInlinePlaceholders(String data, Object... args) {
        return applyInlinePlaceholders(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), this::getInlinePlaceholderValue, args);
    }

    public String applyInlinePlaceholders(String data, Function<PlaceholderSupplier.PlaceholderContext, String> getter, Object... args) {
        return applyInlinePlaceholders(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), getter, args);
    }

    public MComponent applyPlaceholders(MComponent data, Object... args) {
        return parseNext(data, null, args);
    }

    public String applyPlaceholdersFlattened(String text, Object... data) {

        text = applyInlinePlaceholders(text, this::getInlinePlaceholderValue, data);
        text = applyInlinePlaceholders(text, this::getFlattenedPlaceholderValue, data);

        return text;
    }

    public MComponent parseText(String text, Object... data) {

        text = applyInlinePlaceholders(text, this::getInlinePlaceholderValue, data);
        MComponent comp = MComponent.parse(text);
        return applyPlaceholders(comp, data);
    }


    // Inline

    private String applyInlinePlaceholders(InputStream data, Function<PlaceholderSupplier.PlaceholderContext, String> getter, Object... args) {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(data, StandardCharsets.UTF_8));
            String out = parseNextInline(reader, null, getter, args);
            reader.close();
            return out;

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private String parseNextInline(BufferedReader reader, Character earlyTerminate, Function<PlaceholderSupplier.PlaceholderContext, String> getter, Object... args) throws IOException {

        StringBuilder out = new StringBuilder();

        int c;
        while((c = reader.read()) != -1) {

            if (c == '%') {
                out.append(parsePlaceholderInline(reader, getter, args));
            } else if(earlyTerminate != null && c == earlyTerminate) {
                break;
            } else {
                out.appendCodePoint(c);
            }
        }

        return out.toString();
    }

    private String parsePlaceholderInline(BufferedReader reader, Function<PlaceholderSupplier.PlaceholderContext, String> getter, Object... args) throws IOException {

        StringBuilder placeholder = new StringBuilder();
        String parameter = null;

        int c;
        while((c = reader.read()) != -1) {
            if(c == '%') {
                break;
            } else if(c == '<') {
                parameter = parseNextInline(reader, '>', getter, args);
            } else {
                placeholder.appendCodePoint(c);
            }
        }

        PlaceholderSupplier.PlaceholderContext ctx = new PlaceholderSupplier.PlaceholderContext(placeholder.toString(), parameter, args);
        return Optional.ofNullable(getter.apply(ctx)).orElseGet(ctx::toRawPlaceholder);
    }


    // Components
    private MComponent parseNext(MComponent input, Character earlyTerminate, Object... args) {

        StringBuilder current = new StringBuilder();
        List<MComponent> components = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(input.getContent().getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

        try {
            int c;
            while ((c = reader.read()) != -1) {

                if (c == '%') {
                    components.add(new MTextComponent(current.toString()));
                    current = new StringBuilder();

                    components.add(parsePlaceholder(reader, args));
                } else if (earlyTerminate != null && c == earlyTerminate) {
                    break;
                } else {
                    current.appendCodePoint(c);
                }
            }
        } catch (IOException ex) {
            // Ignore
        }

        MComponent out;
        if(components.isEmpty()) {
            out = new MTextComponent(current.toString());
            current = new StringBuilder();
        } else {
            out = components.get(0);
        }
        out.getStyle().fillFrom(input.getStyle());

        for(int i = 1 ; i < components.size() ; i++) {
            out.addChild(components.get(i));
        }

        if(current.length() != 0) {
            out.addChild(new MTextComponent(current.toString()));
        }

        for(MComponent comp : input.getChildren()) {
            out.addChild(parseNext(comp, earlyTerminate, args));
        }


        return out;
    }

    private MComponent parsePlaceholder(BufferedReader reader, Object... args) throws IOException {

        StringBuilder placeholder = new StringBuilder();
        String parameter = null;

        int c;
        while((c = reader.read()) != -1) {
            if(c == '%') {
                break;
            } else if(c == '<') {
                parameter = parseNextInline(reader, '>', this::getFlattenedPlaceholderValue, args);
            } else {
                placeholder.appendCodePoint(c);
            }
        }

        PlaceholderSupplier.PlaceholderContext ctx = new PlaceholderSupplier.PlaceholderContext(placeholder.toString(), parameter, args);
        return Optional.ofNullable(getPlaceholderValue(ctx)).orElseGet(() -> new MTextComponent(ctx.toRawPlaceholder()));
    }

/*    private PlaceholderSupplier.PlaceholderContext createContext(String placeholder, Object... args) {

        StringBuilder name = new StringBuilder();
        String parameter = null;

        char[] arr = placeholder.toCharArray();
        for(int i = 0 ; i < arr.length ; i++) {

            i = readUntil('<', i, arr, name);
            if(i < arr.length) {

                StringBuilder param = new StringBuilder();
                i = readUntil('>', ++i, arr, param);

                parameter = parseInlinePlaceholder(param.toString(), args);
            }
        }
        return new PlaceholderSupplier.PlaceholderContext(name.toString(), parameter, args);
    }

    private String parseInlinePlaceholder(String placeholder, Object... args) {

        PlaceholderSupplier<String> pl = find(CustomPlaceholderInline.class, ci -> ci.getId().equals(placeholder), args);
        PlaceholderSupplier.PlaceholderContext ctx = new PlaceholderSupplier.PlaceholderContext(placeholder, null, args);

        if(pl == null) {
            ctx = createContext(placeholder, args);
            pl = inlinePlaceholders.get(ctx.getName());
        }

        String s = PlaceholderSupplier.get(pl, ctx);
        return s == null ? ctx.toRawPlaceholder() : s;
    }

    private MComponent parsePlaceholder(String placeholder, Object... args) {

        PlaceholderSupplier.PlaceholderContext ctx = createContext(placeholder, args);

        PlaceholderSupplier<MComponent> pl = placeholders.get(ctx.getName());

        CustomPlaceholder cpl = find(CustomPlaceholder.class, ci -> ci.getId().equals(placeholder), args);
        if(cpl != null) pl = cpl;

        MComponent cmp = PlaceholderSupplier.get(pl, ctx);
        return cmp == null ? new MTextComponent(ctx.toRawPlaceholder()) : cmp;
    }

    public String applyInlinePlaceholders(String input, BiFunction<String, Object[], String> getter, Object... args) {
        StringBuilder out = new StringBuilder();

        char[] arr = input.toCharArray();
        for(int i = 0 ; i < arr.length ; i++) {

            i = readUntil('%', i, arr, out);
            if(i < arr.length) {

                StringBuilder placeholder = new StringBuilder();
                i = readUntil('%', ++i, arr, placeholder);
                if(placeholder.length() == 0) {
                    out.append("%%");
                } else {
                    String str = i == arr.length ? "%" + placeholder : getter.apply(placeholder.toString(), args);
                    out.append(str);
                }
            }
        }

        return out.toString();
    }

    public MComponent applyPlaceholders(MComponent input, Object... args) {

        MStyle style = input.getStyle();
        List<MComponent> components = new ArrayList<>();

        char[] arr = input.getContent().toCharArray();
        for(int i = 0 ; i < arr.length ; i++) {

            StringBuilder current = new StringBuilder();
            i = readUntil('%', i, arr, current);
            components.add(new MTextComponent(current.toString()));

            if(i < arr.length) {

                StringBuilder placeholder = new StringBuilder();
                i = readUntil('%', ++i, arr, placeholder);

                if(placeholder.length() == 0) {
                    components.get(components.size() - 1).content += "%";
                } else {
                    MComponent pl = i == arr.length ? new MTextComponent("%" + placeholder) : parsePlaceholder(placeholder.toString(), args);
                    components.add(pl);
                }
            }
        }

        MComponent out = components.isEmpty() ? new MTextComponent("") : components.get(0);
        out.getStyle().fillFrom(style);

        for(int i = 1 ; i < components.size() ; i++) {
            out.addChild(components.get(i));
        }

        for(MComponent comp : input.getChildren()) {
            out.addChild(applyPlaceholders(comp, args));
        }

        return out;
    }

    public String applyPlaceholdersFlattened(String text, Object... data) {

        text = applyInlinePlaceholders(text, this::parseInlinePlaceholder, data);
        text = applyInlinePlaceholders(text, (name, args) -> {
            MComponent comp = parsePlaceholder(name, args);
            return comp.getAllContent();
        }, data);

        return text;
    }

    public MComponent parseText(String text, Object... data) {

        text = applyInlinePlaceholders(text, this::parseInlinePlaceholder, data);
        MComponent comp = MComponent.parse(text);
        return applyPlaceholders(comp, data);
    }



    private static int readUntil(char chara, int offset, char[] buffer, StringBuilder out) {

        int i;
        for(i = offset ; i < buffer.length ; i++) {

            if(buffer[i] == chara) return i;
            out.append(buffer[i]);

        }
        return i;
    }*/

    private static <T> T find(Class<T> clazz, Function<T, Boolean> consumer, Object... args) {
        for(Object o : args) {
            if(o == null) continue;
            if(clazz == o.getClass() || clazz.isAssignableFrom(o.getClass())) {
                T obj = clazz.cast(o);
                if(consumer.apply(obj)) return obj;
            }
        }
        return null;
    }

    public static final PlaceholderManager INSTANCE = new PlaceholderManager();
}
