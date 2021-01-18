package me.m1dnightninja.midnightcore.api.config;

public interface ConfigSerializer<T> {
    T deserialize(ConfigSection var1);

    ConfigSection serialize(T var1);
}

