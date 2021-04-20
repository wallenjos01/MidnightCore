package me.m1dnightninja.midnightcore.api.config;

public interface InlineSerializer<T> {

    T deserialize(String s);

    String serialize(T object);
}

