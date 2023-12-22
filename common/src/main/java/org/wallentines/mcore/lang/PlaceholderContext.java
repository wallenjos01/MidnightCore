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

/**
 * Context by which to resolve placeholders. Contains arguments and an optional placeholder parameter.
 */
public class PlaceholderContext {

    private final Component parameter;
    private final List<Object> values = new ArrayList<>();
    private final HashMap<Class<?>, Object> cache = new HashMap<>();
    private final HashMap<String, Either<String, Component>> customCache = new HashMap<>();

    /**
     * Constructs a new placeholder context with no parameter or values, with the global PlaceholderManager
     */
    public PlaceholderContext() {
        this.parameter = null;
    }

    /**
     * Constructs a new placeholder context with the given parameter
     * @param parameter The resolved parameter which will be passed to placeholder suppliers
     */
    public PlaceholderContext(Component parameter) {
        this.parameter = parameter;
    }

    /**
     * Constructs a new placeholder context with the given values
     * @param values The values which will be passed to placeholder suppliers
     */
    public PlaceholderContext(Collection<Object> values) {
        this.parameter = null;
        values.forEach(this::addValue);
    }

    /**
     * Constructs a new placeholder context with the given parameter and values
     * @param parameter The resolved parameter which will be passed to placeholder suppliers
     * @param values The values which will be passed to placeholder suppliers
     */
    public PlaceholderContext(Component parameter, Collection<Object> values) {
        this.parameter = parameter;
        values.forEach(this::addValue);
    }

    /**
     * Makes an exact copy of this context
     * @return A copy of this context
     */
    public PlaceholderContext copy() {
        PlaceholderContext out = new PlaceholderContext(parameter);
        out.values.addAll(values);
        return out;
    }

    /**
     * Makes an exact copy of this context with the given parameter
     * @return A copy of this context
     */
    public PlaceholderContext copy(Component parameter) {
        PlaceholderContext out = new PlaceholderContext(parameter);
        out.values.addAll(values);
        return out;
    }

    /**
     * Gets the resolved parameter passed to this placeholder
     * @return The resolved parameter
     */
    public Component getParameter() {
        return parameter;
    }

    /**
     * Gets the context values available for this placeholder
     * @return The available context values
     */
    public List<Object> getValues() {
        return List.copyOf(values);
    }

    /**
     * Adds a value to the context
     * @param object The object to add
     */
    public void addValue(Object object) {

        if(object == null) return;

        if(object instanceof CustomPlaceholder) {
            CustomPlaceholder cpl = (CustomPlaceholder) object;
            customCache.putIfAbsent(cpl.getId(), cpl.getValue());
        }

        values.add(object);
        cache.putIfAbsent(object.getClass(), object);
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
            for (Object value : values) {
                if(value == null) continue;
                if (k.isAssignableFrom(value.getClass())) {
                    return k.cast(value);
                }
            }
            return null;
        }));
    }

    public Either<String, Component> getCustomPlaceholder(String key) {

        return customCache.get(key);
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
