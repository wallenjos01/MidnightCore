package me.m1dnightninja.midnightcore.api.config;

public interface ConfigSerializer<T> {

    T deserialize(ConfigSection sec);

    ConfigSection serialize(T obj);

}
