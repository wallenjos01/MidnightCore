package me.m1dnightninja.midnightcore.api.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigSection {

    private final ConfigRegistry reg;
    private final HashMap<String, Object> entries = new HashMap<>();

    public ConfigSection() {
        this(MidnightCoreAPI.getInstance().getConfigRegistry());
    }

    public ConfigSection(ConfigRegistry reg) {
        this.reg = reg;
    }

    @SuppressWarnings("unchecked")
    public <T> void set(String key, T obj) {

        if(obj == null) {
            entries.remove(key);
        } else if(reg.canSerialize(obj.getClass())) {
            entries.put(key, reg.getSerializer((Class<T>) obj.getClass()).serialize(obj));
        } else {
            entries.put(key, obj);
        }
    }

    public Object get(String key) {
        return entries.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {

        Object out = get(key);
        if(!clazz.isAssignableFrom(out.getClass())) {
            throw new IllegalStateException("Invalid Type!");
        }

        return (T) out;
    }

    public boolean has(String key) {
        return entries.containsKey(key);
    }

    public String getString(String key) {
        return get(key, String.class);
    }

    public float getInt(String key) {
        return get(key, int.class);
    }

    public float getFloat(String key) {
        return get(key, float.class);
    }

    public double getDouble(String key) {
        return get(key, double.class);
    }

    public List<?> getList(String key) {
        return get(key, List.class);
    }

    public List<String> getStringList(String key) {

        List<?> orig = getList(key);
        List<String> out = new ArrayList<>();

        for(Object o : orig) {
            out.add(o.toString());
        }

        return out;
    }

    public ConfigSection getSection(String key) {
       return get(key, ConfigSection.class);
    }



}
