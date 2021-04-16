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
        } else if (reg != null && reg.canSerialize(obj.getClass())) {
            this.entries.put(key, reg.getSerializer((Class<T>) obj.getClass()).serialize(obj));
        } else {
            this.entries.put(key, obj);
        }
    }

    public Object get(String key) {
        return this.entries.get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object out = this.get(key);

        return convert(out, clazz);
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

    public <T> List<T> getList(String key, Class<T> clazz) {

        List<?> lst = getList(key);
        List<T> out = new ArrayList<>();
        for(Object o : lst) {
            out.add(convert(o, clazz));
        }

        return out;
    }

    public ConfigSection getSection(String key) {
        return this.get(key, ConfigSection.class);
    }

    public ConfigSection getOrCreateSection(String key) {
        if(has(key, ConfigSection.class)) {
            return getSection(key);
        }
        ConfigSection sec = new ConfigSection();
        set(key, sec);
        return sec;
    }

    public void fill(ConfigSection other) {
        for(Map.Entry<String, Object> ents : other.getEntries().entrySet()) {
            if(!has(ents.getKey(), ents.getValue().getClass())) {
                set(ents.getKey(), ents.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(Object o, Class<T> clazz) {

        if(reg != null && reg.canSerialize(clazz) && o instanceof ConfigSection) {
            ConfigSerializer<T> ser = reg.getSerializer(clazz);
            T ret = ser.deserialize((ConfigSection) o);
            if(ret == null) {
                throw new IllegalStateException("Invalid Type! " + o.getClass().getName() + " cannot be converted to " + clazz.getName());
            }

            return ret;
        } else if (!clazz.isAssignableFrom(o.getClass())) {
            throw new IllegalStateException("Invalid Type! " + o.getClass().getName() + " cannot be converted to " + clazz.getName());
        }

        return (T) o;
    }

}

