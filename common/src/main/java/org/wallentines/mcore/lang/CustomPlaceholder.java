package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Either;

import java.util.Objects;
import java.util.function.Supplier;

public class CustomPlaceholder {

    private final String id;
    private final Supplier<Either<String, Component>> value;

    public CustomPlaceholder(String id, Supplier<Either<String, Component>> value) {
        this.value = value;
        this.id = id;
    }


    public static CustomPlaceholder of(String id, Component value) {
        return new CustomPlaceholder(id, () -> Either.right(value));
    }

    public static CustomPlaceholder inline(String id, String value) {
        return new CustomPlaceholder(id, () -> Either.left(value));
    }

    public static CustomPlaceholder inline(String id, Object value) {
        return new CustomPlaceholder(id, () -> Either.left(Objects.toString(value)));
    }

    public static CustomPlaceholder of(String id, Supplier<Component> value) {
        return new CustomPlaceholder(id, () -> Either.right(value.get()));
    }

    public static CustomPlaceholder inline(String id, Supplier<String> value) {
        return new CustomPlaceholder(id, () -> Either.left(value.get()));
    }

    public Either<String, Component> getValue() {
        return value.get();
    }

    public String getId() {
        return id;
    }
}
