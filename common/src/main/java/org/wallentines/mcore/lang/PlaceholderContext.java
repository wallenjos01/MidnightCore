package org.wallentines.mcore.lang;

import org.jetbrains.annotations.Nullable;
import org.wallentines.mcore.text.Component;
import org.wallentines.midnightlib.types.Either;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PlaceholderContext {

    public Component parameter;
    public final List<Object> values = new ArrayList<>();
    private final HashMap<Class<?>, Object> cache = new HashMap<>();

    public PlaceholderContext() { }

    /**
     * Constructs a new placeholder context with the given values
     * @param values The values which will be passed to placeholder suppliers
     */
    public PlaceholderContext(Collection<Object> values) {
        this.values.addAll(values);
    }

    /**
     * Makes an exact copy of this context
     * @return A copy of this context
     */
    public PlaceholderContext copy() {
        PlaceholderContext out = new PlaceholderContext();
        out.values.addAll(values);
        out.parameter = parameter;
        return out;
    }

    /**
     * Finds an object of type T and returns it, or null
     * @param clazz The class of type of object to look up
     * @return The first object in {@link #values values} with type T
     * @param <T> The type of object to look up
     */
    @Nullable
    public <T> T getValue(Class<T> clazz) {

        return clazz.cast(cache.computeIfAbsent(clazz, k -> {
            for(Object value : values) {
                if(k.isAssignableFrom(value.getClass())) {
                    return k.cast(value);
                }
            }
            return null;
        }));
    }

    public Either<String, Component> getCustomPlaceholder(String key) {
        for(Object value : values) {
            if(value instanceof CustomPlaceholder) {
                CustomPlaceholder cpl = (CustomPlaceholder) value;
                if(key.equals(cpl.getId())) {
                    return cpl.getValue();
                }
            }
        }
        return null;
    }


    /**
     * Finds an object of type T and runs a function on it
     * @param clazz The class of type of object to look up
     * @param func The function to run on the first object of type T
     * @param <T> The type of object to look up
     */
    public <T> void onValue(Class<T> clazz, Consumer<T> func) {

        T val = getValue(clazz);
        if(val != null) func.accept(val);
    }

    /**
     * Finds an object of type T and runs a function on it, returning a value
     * @param clazz The class of type of object to look up
     * @param func The function to run on the first object of type T
     * @param <T> The type of object to look up
     */
    public <T, R> R onValue(Class<T> clazz, Function<T, R> func) {
        T val = getValue(clazz);
        if(val != null) return func.apply(val);
        return null;
    }

    /**
     * Finds an object of type T and runs a function on it, returning a value
     * @param clazz The class of type of object to look up
     * @param func The function to run on the first object of type T
     * @param defaultValue The value to return if no object was found
     * @param <T> The type of object to look up
     */
    public <T, R> R onValueOr(Class<T> clazz, Function<T, R> func, R defaultValue) {
        T val = getValue(clazz);
        if(val != null) return func.apply(val);
        return defaultValue;
    }

    /**
     * Finds an object of type T and runs a function on it, returning a value
     * @param clazz The class of type of object to look up
     * @param func The function to run on the first object of type T
     * @param defaultGetter The function to run if no object was found
     * @param <T> The type of object to look up
     */
    public <T, R> R onValueOr(Class<T> clazz, Function<T, R> func, Supplier<R> defaultGetter) {
        T val = getValue(clazz);
        if(val != null) return func.apply(val);
        return defaultGetter.get();
    }

}
