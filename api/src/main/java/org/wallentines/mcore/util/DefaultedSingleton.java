package org.wallentines.mcore.util;

/**
 * A {@link org.wallentines.mcore.util.Singleton Singleton} which will return a default value if it doesn't contain
 * a value.
 * @param <T> The type of value stored in the Singleton
 */
public class DefaultedSingleton<T> extends Singleton<T> {

    private final T defaultValue;

    /**
     * Constructs a DefaultedSingleton with the given default value
     * @param defaultValue The value to return if a permanent value has not been assigned yet
     */
    public DefaultedSingleton(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the default value of the Singleton, regardless of whether a permanent one has been assigned yet
     * @return The default value of the Singleton
     */
    public T getDefaultValue() {
        return defaultValue;
    }


    @Override
    public T get() throws IllegalStateException {
        return value == null ? defaultValue : value;
    }

    @Override
    public T getOrNull() throws IllegalStateException {
        return value == null ? defaultValue : value;
    }
}
