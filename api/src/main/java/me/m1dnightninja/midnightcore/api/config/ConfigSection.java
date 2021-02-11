package me.m1dnightninja.midnightcore.api.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

public class ConfigSection {
    private final ConfigRegistry reg;
    private final HashMap<String, Object> entries = new HashMap<>();

    public ConfigSection() {
        this(MidnightCoreAPI.getConfigRegistry());
    }

    public ConfigSection(ConfigRegistry reg) {
        this.reg = reg;
    }

    @SuppressWarnings("unchecked")
    public <T> void set(String key, T obj) {
        if (obj == null) {
            this.entries.remove(key);
        } else if (this.reg.canSerialize(obj.getClass())) {
            this.entries.put(key, this.reg.getSerializer((Class<T>) obj.getClass()).serialize(obj));
        } else {
            this.entries.put(key, obj);
        }
    }

    public Object get(String key) {
        return this.entries.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object out = this.get(key);
        if (!clazz.isAssignableFrom(out.getClass())) {
            throw new IllegalStateException("Invalid Type! " + out.getClass().getName() + " cannot be converted to " + clazz.getName());
        }
        return (T) out;
    }

    public Iterable<String> getKeys() {
        return entries.keySet();
    }

    public Map<String, Object> getEntries() {
        return new HashMap<>(entries);
    }

    public boolean has(String key) {
        return this.entries.containsKey(key);
    }

    public <T> boolean has(String key, Class<T> clazz) {
        Object out = this.get(key);
        if(out == null) return false;

        return clazz.isAssignableFrom(out.getClass());
    }

    public String getString(String key) {
        return this.get(key, String.class);
    }

    public int getInt(String key) {
        return this.get(key, Number.class).intValue();
    }

    public float getFloat(String key) {
        return this.get(key, Number.class).floatValue();
    }

    public double getDouble(String key) {
        return this.get(key, Number.class).doubleValue();
    }

    public boolean getBoolean(String key) { return (boolean) this.get(key); }

    public List<?> getList(String key) {
        return this.get(key, List.class);
    }

    public List<String> getStringList(String key) {
        List<?> orig = this.getList(key);
        ArrayList<String> out = new ArrayList<>();
        for (Object o : orig) {
            out.add(o.toString());
        }
        return out;
    }

    public ConfigSection getSection(String key) {
        return this.get(key, ConfigSection.class);
    }

    public void fill(ConfigSection other) {
        for(Map.Entry<String, Object> ents : other.getEntries().entrySet()) {
            if(!has(ents.getKey(), ents.getValue().getClass())) {
                set(ents.getKey(), ents.getValue());
            }
        }
    }

}

