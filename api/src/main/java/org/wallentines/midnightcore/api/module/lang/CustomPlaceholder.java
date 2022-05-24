package org.wallentines.midnightcore.api.module.lang;

import org.wallentines.midnightcore.api.text.MComponent;

public interface CustomPlaceholder {

    MComponent get();
    String getId();

    static CustomPlaceholder create(String id, MComponent out) {
        return new CustomPlaceholder() {
            @Override public MComponent get() { return out; }
            @Override public String getId() { return id; }
        };
    }

}
