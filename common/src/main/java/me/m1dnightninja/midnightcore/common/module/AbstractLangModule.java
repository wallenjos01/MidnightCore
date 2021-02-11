package me.m1dnightninja.midnightcore.common.module;

import java.util.HashMap;

import me.m1dnightninja.midnightcore.api.lang.AbstractLangProvider;
import me.m1dnightninja.midnightcore.api.module.ILangModule;

public abstract class AbstractLangModule<T> implements ILangModule<T> {
    protected final HashMap<String, AbstractLangProvider> providers = new HashMap<>();
    protected final HashMap<String, PlaceholderSupplier<T>> rawPlaceholders = new HashMap<>();
    protected final HashMap<String, PlaceholderSupplier<String>> stringPlaceholders = new HashMap<>();

    @Override
    public String getId() {
        return "lang";
    }

    @Override
    public void registerStringPlaceholder(String key, PlaceholderSupplier<String> supplier) {
        if (!this.stringPlaceholders.containsKey(key)) {
            this.stringPlaceholders.put(key, supplier);
        }
    }

    @Override
    public void registerRawPlaceholder(String key, PlaceholderSupplier<T> supplier) {
        if (!this.rawPlaceholders.containsKey(key)) {
            this.rawPlaceholders.put(key, supplier);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P, O> PlaceholderSupplier<P> createSupplier(Class<O> clazz, TypedSupplier<P, O> supp) {
        return objs -> {
            for(Object o : objs) {
                if(o == null) continue;
                if(clazz.isAssignableFrom(o.getClass())) {
                    try {
                        return supp.get((O) o);
                    } catch(Exception ex) {
                        return null;
                    }
                }
            }
            return null;
        };
    }

    @Override
    public String getStringPlaceholderValue(String key, Object ... args) {

        PlaceholderSupplier<String> supp = stringPlaceholders.get(key);
        if(supp == null) return "%" + key + "%";

        return supp.get(args);
    }

    @Override
    public T getRawPlaceholderValue(String key, Object ... args) {

        PlaceholderSupplier<T> supp = rawPlaceholders.get(key);
        if(supp == null) return null;

        return supp.get(args);
    }
}

