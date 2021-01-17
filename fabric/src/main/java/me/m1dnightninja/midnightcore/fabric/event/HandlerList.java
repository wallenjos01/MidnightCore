package me.m1dnightninja.midnightcore.fabric.event;

import java.util.HashMap;

public class HandlerList<T extends Event> {

    private final HashMap<Object, EventHandler<T>> handlers = new HashMap<>();

    void add(Object o, EventHandler<T> handler) {
        handlers.put(o, handler);
    }

    void invoke(T event) {
        for(EventHandler<T> handler : handlers.values()) {
            handler.invoke(event);
        }
    }

    void clear() {
        handlers.clear();
    }

    void clear(Object o) {
        handlers.remove(o);
    }

}
