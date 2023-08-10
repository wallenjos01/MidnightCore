package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Either;

public class CustomPlaceholder {

    private final Either<String, Component> value;
    private final String id;

    public CustomPlaceholder(String id, String value) {
        this.value = Either.left(value);
        this.id = id;
    }

    public CustomPlaceholder(String id, Component value) {
        this.value = Either.right(value);
        this.id = id;
    }

    public Either<String, Component> getValue() {
        return value;
    }

    public String getId() {
        return id;
    }
}
