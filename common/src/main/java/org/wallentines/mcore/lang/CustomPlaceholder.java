package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Either;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A custom placeholder which can be added to a placeholder context for resolution. Designed for one-off situations
 * where placeholder values are only available in context, or where using the global registry would not be practical.
 */
public class CustomPlaceholder {

    private final String id;
    private final Supplier<Either<String, Component>> value;

    /**
     * Creates a new custom placeholder with the given ID and value supplier
     * @param id The id of the placeholder (The text that appears between two % signs)
     * @param value Called when the value of the placeholder is queried, can return either a String or a Component
     */
    public CustomPlaceholder(String id, Supplier<Either<String, Component>> value) {
        this.value = value;
        this.id = id;
    }


    /**
     * Creates a basic custom placeholder which always returns a Component
     * @param id The id of the placeholder
     * @param value The value to return
     * @return A new custom placeholder
     */
    public static CustomPlaceholder of(String id, Component value) {
        return new CustomPlaceholder(id, () -> Either.right(value));
    }

    /**
     * Creates a basic custom placeholder which always returns a String
     * @param id The id of the placeholder
     * @param value The value to return
     * @return A new custom placeholder
     */
    public static CustomPlaceholder inline(String id, String value) {
        return new CustomPlaceholder(id, () -> Either.left(value));
    }

    /**
     * Creates a basic custom placeholder which always returns a String
     * @param id The id of the placeholder
     * @param value The value convert to a String and return
     * @return A new custom placeholder
     */
    public static CustomPlaceholder inline(String id, Object value) {
        return new CustomPlaceholder(id, () -> Either.left(Objects.toString(value)));
    }

    /**
     * Creates a custom placeholder which supplies a Component  when resolved
     * @param id The id of the placeholder
     * @param value The function to call to get the value
     * @return A new custom placeholder
     */
    public static CustomPlaceholder of(String id, Supplier<Component> value) {
        return new CustomPlaceholder(id, () -> Either.right(value.get()));
    }

    /**
     * Creates a custom placeholder which supplies a String when resolved
     * @param id The id of the placeholder
     * @param value The function to call to get the value
     * @return A new custom placeholder
     */
    public static CustomPlaceholder inline(String id, Supplier<String> value) {
        return new CustomPlaceholder(id, () -> Either.left(value.get()));
    }

    /**
     * Gets the value of the placeholder
     * @return Either a String or a Component
     */
    public Either<String, Component> getValue() {
        return value.get();
    }

    /**
     * Gets the ID of the placeholder
     * @return The placeholder's ID
     */
    public String getId() {
        return id;
    }
}
