package me.m1dnightninja.midnightcore.api.config;

public interface ConfigSerializer<T> {

    T deserialize(ConfigSection section);

    ConfigSection serialize(T object);
}

