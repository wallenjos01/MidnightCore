package org.wallentines.midnightcore.api.module.lang;

public interface CustomPlaceholderInline {

    String get();
    String getId();

    static CustomPlaceholderInline create(String id, String out) {
        return new CustomPlaceholderInline() {
            @Override public String get() { return out; }
            @Override public String getId() { return id; }
        };
    }

}
