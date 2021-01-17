package me.m1dnightninja.midnightcore.api.config;

import java.util.HashMap;

public class ConfigRegistry {


    private final HashMap<Class<?>, ConfigSerializer<?>> serializers = new HashMap<>();


    public <T> void registerSerializer(Class<T> clazz, ConfigSerializer<T> serializer) {
        serializers.put(clazz, serializer);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigSerializer<T> getSerializer(Class<T> clazz) {
        if(!serializers.containsKey(clazz)) return null;
        return (ConfigSerializer<T>) serializers.get(clazz);
    }

    public boolean canSerialize(Class<?> clazz) {
        return serializers.containsKey(clazz);
    }

}
