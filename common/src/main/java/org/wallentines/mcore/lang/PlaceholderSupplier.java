package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Either;

import java.util.function.Function;

/**
 * A functional interface for resolving a specific type of placeholder according to a given context
 */
public interface PlaceholderSupplier {

    /**
     * Resolves a placeholder according to the given context
     * @param context The context by which to resolve
     * @return A resolved placeholder. May either be a String or a Component.
     */
    Either<String, Component> get(PlaceholderContext context);

    /**
     * Creates a new basic placeholder supplier which returns a Component
     * @param func The function to call when resolving
     * @return A new placeholder supplier
     */
    static PlaceholderSupplier of(Function<PlaceholderContext, Component> func) {
        return context -> Either.right(func.apply(context));
    }


    /**
     * Creates a new basic inline placeholder supplier which returns a String
     * @param func The function to call when resolving
     * @return A new placeholder supplier
     */
    static PlaceholderSupplier inline(Function<PlaceholderContext, String> func) {
        return context -> Either.left(func.apply(context));
    }

}
