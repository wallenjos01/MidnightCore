package org.wallentines.midnightcore.api.text;

public interface CustomPlaceholder extends PlaceholderSupplier<MComponent> {

    MComponent get();

    String getId();

    @Override
    default MComponent get(PlaceholderContext ctx) {
        return get();
    }

    @Override
    default boolean acceptsParameters() {
        return false;
    }

    static CustomPlaceholder create(String id, MComponent out) {
        return new CustomPlaceholder() {
            @Override public MComponent get() { return out; }
            @Override public String getId() { return id; }
        };
    }

    static CustomPlaceholderInline create(String id, String out) {
        return new CustomPlaceholderInline() {
            @Override public String get() { return out; }
            @Override public String getId() { return id; }
        };
    }

}
