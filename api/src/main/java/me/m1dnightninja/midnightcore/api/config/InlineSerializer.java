package me.m1dnightninja.midnightcore.api.config;

public interface InlineSerializer<T> {

    T deserialize(String s);

    String serialize(T object);

    default boolean canDeserialize(String s) {
        T val;
        try {
            val = deserialize(s);
        } catch (Exception ex) {
            return false;
        }
        return val != null;
    }

}

