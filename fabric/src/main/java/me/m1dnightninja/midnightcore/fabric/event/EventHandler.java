package me.m1dnightninja.midnightcore.fabric.event;

public interface EventHandler<T extends Event> {

    void invoke(T event);

}
