package me.m1dnightninja.midnightcore.api.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigRegistry {

    public static final ConfigRegistry INSTANCE = new ConfigRegistry();

    private final HashMap<Class<?>, ConfigSerializer<?>> serializers = new HashMap<>();
    private final HashMap<Class<?>, InlineSerializer<?>> inlineSerializers = new HashMap<>();

    private final List<ConfigProvider> providers = new ArrayList<>();
    private final HashMap<String, Integer> providersByExtension = new HashMap<>();

    private ConfigProvider defaultProvider;

    public ConfigProvider getDefaultProvider() {
        if(defaultProvider == null) {
            return providers.get(0);
        }
        return defaultProvider;
    }

    public void setDefaultProvider(ConfigProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public <T> void registerSerializer(Class<T> clazz, ConfigSerializer<T> serializer) {
        this.serializers.put(clazz, serializer);
    }

    public <T> void registerInlineSerializer(Class<T> clazz, InlineSerializer<T> serializer) {
        this.inlineSerializers.put(clazz, serializer);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigSerializer<T> getSerializer(Class<T> clazz) {
        for(Class<?> ser : serializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return (ConfigSerializer<T>) serializers.get(ser);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> InlineSerializer<T> getInlineSerializer(Class<T> clazz) {
        for(Class<?> ser : inlineSerializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return (InlineSerializer<T>) inlineSerializers.get(ser);
        }

        return null;
    }

    public boolean canSerialize(Class<?> clazz) {

        for(Class<?> ser : serializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return true;
        }

        return false;
    }

    public boolean canSerializeInline(Class<?> clazz) {
        for(Class<?> ser : inlineSerializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return true;
        }

        return false;
    }

    public <T extends ConfigProvider> T registerProvider(T prov) {
        if(providersByExtension.containsKey(prov.getFileExtension())) return null;

        int index = providers.size();
        providers.add(prov);
        providersByExtension.put(prov.getFileExtension(), index);

        return prov;
    }

    public ConfigProvider getProviderForFileType(String extension) {

        Integer index = providersByExtension.get(extension);
        if(index == null) return null;

        return providers.get(index);
    }

    public ConfigProvider getProviderForFile(File f) {

        String name = f.getName();
        if(name.contains(".")) {
            return getProviderForFileType(name.substring(name.lastIndexOf(".")));

        } else {
            return getDefaultProvider();
        }

    }

}

