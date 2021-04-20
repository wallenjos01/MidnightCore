package me.m1dnightninja.midnightcore.api.module.lang;

import java.util.function.Function;

public interface PlaceholderSupplier<T> {

    T get(Object... args);

    @SuppressWarnings("unchecked")
    static <P, T> T runFor(Class<P> clazz, Object[] objs, Function<P, T> run) {

        for(Object o : objs) {
            if(o.getClass() == clazz || o.getClass().isAssignableFrom(clazz)) {
                return run.apply((P) o);
            }
        }
        return null;
    }

}
