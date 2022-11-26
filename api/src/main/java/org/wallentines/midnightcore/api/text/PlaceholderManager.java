package org.wallentines.midnightcore.api.text;

import org.wallentines.midnightlib.registry.StringRegistry;

import java.util.ArrayList;
import java.util.List;
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

    private PlaceholderSupplier.PlaceholderContext createContext(String placeholder, Object... args) {

        StringBuilder name = new StringBuilder();
        String parameter = null;

        char[] arr = placeholder.toCharArray();
        for(int i = 0 ; i < arr.length ; i++) {

            i = readUntil('<', i, arr, name);
            if(i < arr.length) {

                StringBuilder param = new StringBuilder();
                i = readUntil('>', ++i, arr, param);

                parameter = param.toString();
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

    public String applyInlinePlaceholders(String input, Object... args) {
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
                    String str = i == arr.length ? "%" + placeholder : parseInlinePlaceholder(placeholder.toString(), args);
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

    public MComponent parseText(String text, Object... data) {

        text = applyInlinePlaceholders(text, data);
        MComponent comp = MComponent.parse(text);
        return applyPlaceholders(comp, data);
    }

    public String getInlinePlaceholderValue(String key, String parameter, Object... args) {

        CustomPlaceholderInline cp = find(CustomPlaceholderInline.class, pl -> pl.getId().equals(key), args);
        if(cp != null) return cp.get();

        PlaceholderSupplier.PlaceholderContext ctx = new PlaceholderSupplier.PlaceholderContext(key, parameter, args);
        return PlaceholderSupplier.get(inlinePlaceholders.get(key), ctx);
    }

    public MComponent getPlaceholderValue(String key, String parameter, Object... args) {

        CustomPlaceholder cp = find(CustomPlaceholder.class, pl -> pl.getId().equals(key), args);
        if(cp != null) return cp.get();

        PlaceholderSupplier.PlaceholderContext ctx = new PlaceholderSupplier.PlaceholderContext(key, parameter, args);
        return PlaceholderSupplier.get(placeholders.get(key), ctx);
    }

    private static int readUntil(char chara, int offset, char[] buffer, StringBuilder out) {

        int i;
        for(i = offset ; i < buffer.length ; i++) {

            if(buffer[i] == chara) return i;
            out.append(buffer[i]);

        }
        return i;
    }

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
