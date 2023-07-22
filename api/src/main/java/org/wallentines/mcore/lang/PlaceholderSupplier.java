package org.wallentines.mcore.lang;

import org.wallentines.mcore.text.Component;
import org.wallentines.mcore.util.Either;

import java.util.function.Function;

public interface PlaceholderSupplier {

    Either<String, Component> get(PlaceholderContext context);

    default boolean acceptsArgument() {
        return false;
    }

    default boolean isInline(PlaceholderContext ctx) {
        return get(ctx).hasLeft();
    }

    static PlaceholderSupplier of(Function<PlaceholderContext, Component> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.right(func.apply(context));
            }

            @Override
            public boolean isInline(PlaceholderContext ctx) {
                return false;
            }
        };
    }

    static PlaceholderSupplier withParameter(Function<PlaceholderContext, Component> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.right(func.apply(context));
            }

            @Override
            public boolean isInline(PlaceholderContext ctx) {
                return false;
            }

            @Override
            public boolean acceptsArgument() {
                return true;
            }
        };
    }

    static PlaceholderSupplier inline(Function<PlaceholderContext, String> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.left(func.apply(context));
            }

            @Override
            public boolean isInline(PlaceholderContext ctx) {
                return true;
            }
        };
    }

    static PlaceholderSupplier inlineWithParameter(Function<PlaceholderContext, String> func) {
        return new PlaceholderSupplier() {
            @Override
            public Either<String, Component> get(PlaceholderContext context) {
                return Either.left(func.apply(context));
            }

            @Override
            public boolean isInline(PlaceholderContext ctx) {
                return true;
            }

            @Override
            public boolean acceptsArgument() {
                return true;
            }
        };
    }

}
