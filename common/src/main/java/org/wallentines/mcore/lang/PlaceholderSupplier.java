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
     * Determines whether this type of placeholder supports an argument
     * @return Whether arguments can be passed
     */
    default boolean acceptsArgument() {
        return false;
    }

    /**
     * Determines whether this will return a String when resolved according to the given context
     * @param context The context by which to resolve
     * @return Whether this will resolve to a String
     */
    default boolean isInline(PlaceholderContext context) {
        return get(context).hasLeft();
    }


    /**
     * Creates a new basic placeholder supplier which returns a Component
     * @param func The function to call when resolving
     * @return A new placeholder supplier
     */
    static PlaceholderSupplier of(Function<PlaceholderContext, Component> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.right(func.apply(context));
            }
            @Override
            public boolean isInline(PlaceholderContext context) {
                return false;
            }
        };
    }


    /**
     * Creates a new basic placeholder supplier which returns a Component and accepts a parameter
     * @param func The function to call when resolving
     * @return A new placeholder supplier
     */
    static PlaceholderSupplier withParameter(Function<PlaceholderContext, Component> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.right(func.apply(context));
            }
            @Override
            public boolean isInline(PlaceholderContext context) {
                return false;
            }
            @Override
            public boolean acceptsArgument() {
                return true;
            }
        };
    }


    /**
     * Creates a new basic inline placeholder supplier which returns a String
     * @param func The function to call when resolving
     * @return A new placeholder supplier
     */
    static PlaceholderSupplier inline(Function<PlaceholderContext, String> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.left(func.apply(context));
            }
            @Override
            public boolean isInline(PlaceholderContext context) {
                return true;
            }
        };
    }


    /**
     * Creates a new basic inline placeholder supplier which returns a String and accepts a parameter
     * @param func The function to call when resolving
     * @return A new placeholder supplier
     */
    static PlaceholderSupplier inlineWithParameter(Function<PlaceholderContext, String> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.left(func.apply(context));
            }
            @Override
            public boolean isInline(PlaceholderContext context) {
                return true;
            }
            @Override
            public boolean acceptsArgument() {
                return true;
            }
        };
    }

}
