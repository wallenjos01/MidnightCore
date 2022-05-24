package org.wallentines.midnightcore.api.module.lang;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface PlaceholderSupplier<T> {

    T get(Object... args);

    static <P, T> PlaceholderSupplier<T> create(Class<P> clazz, Function<P, T> run) {

        return create(clazz, run, null);
    }

    @SuppressWarnings("unchecked")
    static <P, T> PlaceholderSupplier<T> create(Class<P> clazz, Function<P, T> run, T def) {

        return args -> {
            for(Object o : args) {
                if(o != null && (o.getClass() == clazz || clazz.isAssignableFrom(o.getClass()))) {
                    return run.apply((P) o);
                }
            }
            return def;
        };
    }

    static <T> PlaceholderSupplier<T> create(T out) {
        return args -> out;
    }

    @SuppressWarnings("unchecked")
    static <P, P2, T> PlaceholderSupplier<T> create(Class<P> clazz, Class<P2> clazz2, BiFunction<P, P2, T> run, Function<P, T> onlyFirst, Function<P2, T> onlySecond) {

        return args -> {
            P p1 = null;
            P2 p2 = null;
            for(Object o : args) {
                if(o == null) continue;
                if(o.getClass() == clazz || clazz.isAssignableFrom(o.getClass())) {
                    p1 = (P) o;
                } else if(o.getClass() == clazz2 || clazz2.isAssignableFrom(o.getClass())) {
                    p2 = (P2) o;
                }
            }

            if(p1 == null) return onlySecond.apply(p2);
            if(p2 == null) return onlyFirst.apply(p1);

            return run.apply(p1, p2);
        };

    }

}
