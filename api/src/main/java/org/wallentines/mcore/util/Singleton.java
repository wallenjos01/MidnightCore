package org.wallentines.mcore.util;

import org.jetbrains.annotations.Nullable;
import org.wallentines.midnightlib.event.SingletonHandlerList;

/**
 * A data type which stores a single object of type T. Once created, the singleton may not be populated yet.
 * Once populated, the contained object will never be reset.
 * @param <T> The type of value stored in the Singleton
 */
public class Singleton<T> {

    protected T value;

    /**
     * An event that will be fired when the Singleton is first assigned a value. Handlers registered after the
     * value is assigned will be immediately triggered.
     */
    public final SingletonHandlerList<T> setEvent = new SingletonHandlerList<>();

    /**
     * Gets the value stored in the Singleton, or throws an error if not set yet
     * @return The value stored in the Singleton
     * @throws IllegalStateException If the singleton is incomplete
     */
    public T get() throws IllegalStateException {
        if(this.value == null) throw new IllegalStateException("Attempt to access the value of an incomplete singleton!");
        return this.value;
    }

    /**
     * Gets the value stored in the Singleton, or null if not set yet
     * @return The value stored in the Singleton, or null
     */
    @Nullable
    public T getOrNull() {
        return this.value;
    }

    /**
     * Gets the value stored in the Singleton, or some default value if not set yet
     * @param defaultValue The value to return if no value is set
     * @return The value stored in the Singleton, or the provided default
     */
    public T getOr(T defaultValue) {
        return this.value == null ? defaultValue : this.value;
    }

    /**
     * Assigns the value within the Singleton
     * @param value The value to assign
     * @throws IllegalStateException If the Singleton already has a value
     */
    public void set(T value) throws IllegalStateException {
        if(this.value != null) throw new IllegalStateException("Attempt to assign a value to a completed singleton!");
        this.value = value;
        setEvent.invoke(value);
    }

}
