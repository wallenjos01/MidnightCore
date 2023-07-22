package org.wallentines.mcore.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A data type which contains one of two different types
 * @param <L> One type it can contain
 * @param <R> The other type it can contain
 */
public class Either<L,R> {

    private final L lValue;
    private final R rValue;

    private Either(L lValue, R rValue) {
        this.lValue = lValue;
        this.rValue = rValue;
    }

    /**
     * Returns the left value, or null
     * @return The left value
     */
    @Nullable
    public L left() {
        return lValue;
    }

    /**
     * Returns the right value, or null
     * @return The right value
     */
    @Nullable
    public R right() {
        return rValue;
    }

    /**
     * Returns the left value, or throws an error
     * @return The left value
     * @throws IllegalStateException If there is no left value
     */
    @NotNull
    public L leftOrThrow() throws IllegalStateException {
        if(lValue == null) throw new IllegalStateException("Left value was not present!");
        return lValue;
    }

    /**
     * Returns the right value, or throws an error
     * @return The right value
     * @throws IllegalStateException If there is no right value
     */
    @NotNull
    public R rightOrThrow() throws IllegalStateException {
        if(rValue == null) throw new IllegalStateException("Right value was not present!");
        return rValue;
    }

    /**
     * Returns the left value, or some default value
     * @param defaultValue The value to return if there is no left value
     * @return The left value
     */
    public L leftOr(L defaultValue) {
        return hasLeft() ? lValue : defaultValue;
    }

    /**
     * Returns the right value, or some default value
     * @param defaultValue The value to return if there is no right value
     * @return The right value
     */
    public R rightOr(R defaultValue) {
        return hasRight() ? rValue : defaultValue;
    }

    /**
     * Returns the left value, or calls another getter
     * @param getter The function to call if there is no left value
     * @return The left value
     */
    public L leftOrGet(Function<R, L> getter) {
        return hasLeft() ? lValue : getter.apply(rValue);
    }

    /**
     * Returns the right value, or calls another getter
     * @param getter The function to call if there is no right value
     * @return The right value
     */
    public R rightOrGet(Function<L, R> getter) {
        return hasRight() ? rValue : getter.apply(lValue);
    }

    /**
     * Determines whether there is a left value present
     * @return Whether there is a left value
     */
    public boolean hasLeft() {
        return lValue != null;
    }

    /**
     * Determines whether there is a right value present
     * @return Whether there is a right value
     */
    public boolean hasRight() {
        return rValue != null;
    }

    /**
     * Constructs an Either with only the left value present
     * @param value The value
     * @return A new Either object
     * @param <L> The type of value on the left
     * @param <R> The type of value on the right
     */
    public static <L,R> Either<L, R> left(L value) {
        return new Either<>(value, null);
    }

    /**
     * Constructs an Either with only the right value present
     * @param value The value
     * @return A new Either object
     * @param <L> The type of value on the left
     * @param <R> The type of value on the right
     */
    public static <L,R> Either<L, R> right(R value) {
        return new Either<>(null, value);
    }

}
