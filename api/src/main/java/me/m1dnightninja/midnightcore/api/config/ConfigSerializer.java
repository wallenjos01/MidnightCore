package me.m1dnightninja.midnightcore.api.config;

public interface ConfigSerializer<T> {

    T deserialize(ConfigSection section);

    ConfigSection serialize(T object);

    default boolean canDeserialize(ConfigSection sec) {
        T val;
        try {
            val = deserialize(sec);
        } catch (Exception ex) {
            return false;
        }
        return val != null;
    }
}

