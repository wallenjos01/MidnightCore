package me.m1dnightninja.midnightcore.api.config;

import java.util.HashMap;

public class ConfigRegistry {

    private final HashMap<Class<?>, ConfigSerializer<?>> serializers = new HashMap<>();
    private final HashMap<Class<?>, InlineSerializer<?>> inlineSerializers = new HashMap<>();
    private final HashMap<String, ConfigProvider> providers = new HashMap<>();

    public <T> void registerSerializer(Class<T> clazz, ConfigSerializer<T> serializer) {
        this.serializers.put(clazz, serializer);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigSerializer<T> getSerializer(Class<T> clazz) {
        if (!this.serializers.containsKey(clazz)) {
            return null;
        }
        return (ConfigSerializer<T>) this.serializers.get(clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> InlineSerializer<T> getInlineSerializer(Class<T> clazz) {
        if (!this.inlineSerializers.containsKey(clazz)) {
            return null;
        }
        return (InlineSerializer<T>) this.inlineSerializers.get(clazz);
    }

    public boolean canSerialize(Class<?> clazz) {
        return this.serializers.containsKey(clazz);
    }

    public boolean canSerializeInline(Class<?> clazz) {
        return this.inlineSerializers.containsKey(clazz);
    }

    public void registerProvider(ConfigProvider prov) {
        if(providers.containsKey(prov.getFileExtension())) return;

        this.providers.put(prov.getFileExtension(), prov);
    }

    public ConfigProvider getProfiderForFileType(String extension) {
        return providers.get(extension);
    }
}

