package me.m1dnightninja.midnightcore.fabric.event;

import org.jetbrains.annotations.NotNull;

import java.util.PriorityQueue;

public class HandlerList<T extends Event> {

    private final PriorityQueue<WrappedHandler> handlers = new PriorityQueue<>();

    void add(Object o, int priority, EventHandler<T> handler) {

        WrappedHandler hand = new WrappedHandler();
        hand.handler = handler;
        hand.priority = priority;
        hand.listener = o;

        handlers.add(hand);
    }

    void invoke(T event) {
        for(WrappedHandler handler : handlers) {
            handler.handler.invoke(event);
        }
    }

    void clear() {
        handlers.clear();
    }

    void clear(Object o) {
        handlers.removeIf(wrappedHandler -> wrappedHandler.listener == o);
    }

    private class WrappedHandler implements Comparable<WrappedHandler> {

        Object listener;
        int priority;
        EventHandler<T> handler;

        @Override
        public int compareTo(@NotNull HandlerList<T>.WrappedHandler o) {
            return priority - o.priority;
        }
    }

}
