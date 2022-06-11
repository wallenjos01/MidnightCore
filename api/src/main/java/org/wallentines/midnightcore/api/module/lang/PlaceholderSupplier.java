package org.wallentines.midnightcore.api.module.lang;

import org.wallentines.midnightlib.config.serialization.Functions;

import java.util.function.Supplier;

public interface PlaceholderSupplier<T> {

    T get(PlaceholderContext ctx);

    default boolean acceptsParameters() { return true; }

    static <P, T> PlaceholderSupplier<T> create(Class<P> clazz, Functions.Function1<P, T> run) {
        return create(clazz, run, () -> null);
    }

    @SuppressWarnings("unchecked")
    static <P, T> PlaceholderSupplier<T> create(Class<P> clazz, Functions.Function1<P, T> run, Supplier<T> def) {

        return new PlaceholderSupplier<>() {
            @Override
            public T get(PlaceholderContext ctx) {

                for(Object o : ctx.args) {
                    if(o != null && (o.getClass() == clazz || clazz.isAssignableFrom(o.getClass()))) {
                        return run.create((P) o);
                    }
                }
                return def.get();
            }
            public boolean acceptsParameters() {
                return false;
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <P, T> PlaceholderSupplier<T> createWithParameter(Class<P> clazz, Functions.Function2<P, String, T> run, Functions.Function1<String, T> def) {

        return ctx -> {

            for(Object o : ctx.args) {
                if(o != null && (o.getClass() == clazz || clazz.isAssignableFrom(o.getClass()))) {
                    return run.create((P) o, ctx.parameter);
                }
            }
            return def.create(ctx.getParameter());
        };
    }

    static <T> PlaceholderSupplier<T> create(T out) {
        return args -> out;
    }

    static <P, P2, T> PlaceholderSupplier<T> create(Class<P> clazz, Class<P2> clazz2, Functions.Function2<P, P2, T> run, Functions.Function1<P, T> onlyFirst, Functions.Function1<P2, T> onlySecond, Supplier<T> def) {

        return new PlaceholderSupplier<>() {
            @Override
            public T get(PlaceholderContext ctx) {
                Tuple<P, P2> p = extractParameters(clazz, clazz2, ctx);

                if(p.x == null && p.y == null) return def.get();

                if(p.x == null) return onlySecond.create(p.y);
                if(p.y == null) return onlyFirst.create(p.x);

                return run.create(p.x, p.y);
            }

            @Override
            public boolean acceptsParameters() {
                return false;
            }
        };
    }

    static <P, P2, T> PlaceholderSupplier<T> createWithParameter(Class<P> clazz, Class<P2> clazz2, Functions.Function3<P, P2, String, T> run, Functions.Function2<P, String, T> onlyFirst, Functions.Function2<P2, String, T> onlySecond, Functions.Function1<String, T> def) {

        return ctx -> {

            Tuple<P, P2> p = extractParameters(clazz, clazz2, ctx);

            if(p.x == null && p.y == null) return def.create(ctx.getParameter());

            if(p.x == null) return onlySecond.create(p.y, ctx.getParameter());
            if(p.y == null) return onlyFirst.create(p.x, ctx.getParameter());

            return run.create(p.x, p.y, ctx.getParameter());
        };
    }

    static <T> T get(PlaceholderSupplier<T> supp, PlaceholderContext ctx) {

        if(supp == null) return null;

        if((ctx.parameter == null) == supp.acceptsParameters()) throw new IllegalArgumentException(
                supp.acceptsParameters() ?
                    "This placeholder requires a parameter, but none is provided!" :
                    "This placeholder does not accept parameters!"
                );

        return supp.get(ctx);
    }

    @SuppressWarnings("unchecked")
    private static <T1, T2> Tuple<T1, T2> extractParameters(Class<T1> clazz, Class<T2> clazz2, PlaceholderContext ctx) {

        T1 p1 = null;
        T2 p2 = null;
        for(Object o : ctx.getArgs()) {
            if(o == null) continue;
            if(o.getClass() == clazz || clazz.isAssignableFrom(o.getClass())) {
                p1 = (T1) o;
            } else if(o.getClass() == clazz2 || clazz2.isAssignableFrom(o.getClass())) {
                p2 = (T2) o;
            }
        }

        return new Tuple<>(p1, p2);
    }

    class PlaceholderContext {

        private final String name;
        private final Object[] args;
        private final String parameter;

        public PlaceholderContext(String name, Object[] args, String parameter) {
            this.name = name;
            this.args = args;
            this.parameter = parameter;
        }

        public String getName() {
            return name;
        }

        public Object[] getArgs() {
            return args;
        }

        public String getParameter() {
            return parameter;
        }

        public String toRawPlaceholder() {
            StringBuilder out = new StringBuilder("%");
            out.append(name);
            if(parameter != null) {
                out.append('<').append(parameter).append('>');
            }
            out.append('%');
            return out.toString();
        }
    }

    class Tuple<X, Y> {

        X x;
        Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

}
