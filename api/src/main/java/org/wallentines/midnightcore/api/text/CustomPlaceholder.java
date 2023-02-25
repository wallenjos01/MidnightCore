package org.wallentines.midnightcore.api.text;

import java.util.function.Function;

public interface CustomPlaceholder extends PlaceholderSupplier<MComponent> {

    String getId();

    @Override
    default boolean acceptsParameters() {
        return false;
    }

    static CustomPlaceholder create(String id, MComponent out) {
        return new CustomPlaceholder() {
            @Override public MComponent get(PlaceholderContext ctx) { return out; }
            @Override public String getId() { return id; }
        };
    }

    @Deprecated
    static CustomPlaceholderInline create(String id, String out) {
        return createInline(id, out);
    }

    static CustomPlaceholderInline createInline(String id, String out) {
        return new CustomPlaceholderInline() {
            @Override public String get(PlaceholderContext ctx) { return out; }
            @Override public String getId() { return id; }
        };
    }

    static CustomPlaceholder create(String id, Function<PlaceholderContext, MComponent> out) {
        return new CustomPlaceholder() {
            @Override public MComponent get(PlaceholderContext ctx) { return out.apply(ctx); }
            @Override public String getId() { return id; }
        };
    }

    static CustomPlaceholder createInline(String id, Function<PlaceholderContext, MComponent> out) {
        return new CustomPlaceholder() {
            @Override public MComponent get(PlaceholderContext ctx) { return out.apply(ctx); }
            @Override public String getId() { return id; }
        };
    }


}
