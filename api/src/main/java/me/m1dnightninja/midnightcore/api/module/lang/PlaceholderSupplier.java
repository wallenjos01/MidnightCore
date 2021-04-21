package me.m1dnightninja.midnightcore.api.module.lang;

import java.util.function.Function;

public interface PlaceholderSupplier<T> {

    T get(Object... args);

    @SuppressWarnings("unchecked")
    static <P, T> PlaceholderSupplier<T> create(Class<P> clazz, Function<P, T> run) {

        return args -> {
            for(Object o : args) {
                if(o.getClass() == clazz || o.getClass().isAssignableFrom(clazz)) {
                    return run.apply((P) o);
                }
            }
            return null;
        };
    }

}
