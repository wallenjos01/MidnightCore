package me.m1dnightninja.midnightcore.fabric.event;

import me.m1dnightninja.midnightcore.fabric.MidnightCore;

import java.util.HashMap;

public class Event {

    private static final HashMap<Class<? extends Event>, HandlerList<? extends Event>> events = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends Event> void register(Class<T> ev, Object listener, EventHandler<T> handler) {

        if(listener == null || handler == null) return;

        events.computeIfAbsent(ev, k -> new HandlerList<T>());

        HandlerList<T> list = (HandlerList<T>) events.get(ev);
        list.add(listener, handler);

    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void invoke(T event) {

        if(event == null) return;

        HandlerList<T> handlers = (HandlerList<T>) events.get(event.getClass());
        if(handlers == null) return;

        handlers.invoke(event);

    }

    public static void unregisterAll(Object o) {

        for(HandlerList<?> l : events.values()) {
            l.clear(o);
        }
    }

    public static void unregisterAll(Class<? extends Event> event) {

        if(events.containsKey(event)) {
            events.get(event).clear();
        }
    }

}
