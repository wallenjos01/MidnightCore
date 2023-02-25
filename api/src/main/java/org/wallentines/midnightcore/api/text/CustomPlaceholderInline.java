package org.wallentines.midnightcore.api.text;

public interface CustomPlaceholderInline extends PlaceholderSupplier<String> {

    String getId();

    @Override
    default boolean acceptsParameters() {
        return false;
    }

    static CustomPlaceholderInline create(String id, String out) {
        return new CustomPlaceholderInline() {
            @Override public String get(PlaceholderContext ctx) { return out; }
            @Override public String getId() { return id; }
        };
    }

}
