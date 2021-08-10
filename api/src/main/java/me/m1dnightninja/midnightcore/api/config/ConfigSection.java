package me.m1dnightninja.midnightcore.api.config;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LinkedTreeMap;
import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

public class ConfigSection {
    private final ConfigRegistry reg;
    private final LinkedTreeMap<String, Object> entries = new LinkedTreeMap<>();

    public ConfigSection() {
        this(MidnightCoreAPI.getInstance().getConfigRegistry());
    }

    public ConfigSection(ConfigRegistry reg) {
        this.reg = reg;
    }

    public <T> void set(String key, T obj) {
        // Remove an object
        if (obj == null) {

            this.entries.remove(key);

        } else {

            this.entries.put(key, serialize(obj));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Object serialize(T obj) {

        // Try to Serialize Map
        if (obj instanceof Map) {

            HashMap<String, Object> out = new HashMap<>();
            for (Map.Entry<?, ?> ent : ((Map<?, ?>) obj).entrySet()) {
                out.put(ent.getKey().toString(), serialize(ent.getValue()));
            }
            return out;

        // Try to serialize List elements
        } else if(obj instanceof List) {
            List<Object> serialized = new ArrayList<>();
            for(Object o : (List<?>) obj) {
                serialized.add(serialize(o));
            }
            return serialized;

        // Try to serialize as a ConfigSection
        } else if (reg != null && reg.canSerialize(obj.getClass())) {

            return reg.getSerializer((Class<T>) obj.getClass()).serialize(obj);

        // Try to serialize as a String
        } else if(reg != null && reg.canSerializeInline(obj.getClass())) {

            return reg.getInlineSerializer((Class<T>) obj.getClass()).serialize(obj);
        }
        // Return the raw data if we cannot serialize
        return obj;
    }

    public void setMap(String id, String keyLabel, String valueLabel, Map<?, ?> map) {

        List<ConfigSection> lst = new ArrayList<>();
        for(Map.Entry<?, ?> ent : map.entrySet()) {
            ConfigSection sec = new ConfigSection();
            sec.set(keyLabel, ent.getKey());
            sec.set(valueLabel, ent.getValue());
            lst.add(sec);
        }

        set(id, lst);
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

        return canConvert(out, clazz);
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

    public long getLong(String key) { return this.get(key, Number.class).longValue(); }

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
        List<T> out = new ArrayList<>(lst.size());
        for(Object o : lst) {
            out.add(convert(o, clazz));
        }

        return out;
    }

    public <T> List<T> getListFiltered(String key, Class<T> clazz) {

        List<?> lst = getList(key);
        List<T> out = new ArrayList<>();
        for(Object o : lst) {
            if(!canConvert(o, clazz)) continue;
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
        for(Map.Entry<String, Object> entry : other.getEntries().entrySet()) {
            if(!has(entry.getKey())) {
                set(entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(Object o, Class<T> clazz) {

        if(reg != null) {
            if(reg.canSerialize(clazz) && o instanceof ConfigSection) {
                ConfigSerializer<T> ser = reg.getSerializer(clazz);
                T ret = ser.deserialize((ConfigSection) o);
                if (ret == null) {
                    throw new IllegalStateException("Invalid Type! " + o.getClass().getName() + " cannot be converted to " + clazz.getName());
                }
                return ret;
            }
            if(reg.canSerializeInline(clazz)) {

                InlineSerializer<T> ser = reg.getInlineSerializer(clazz);
                T ret = ser.deserialize(o.toString());
                if (ret == null) {
                    throw new IllegalStateException("Invalid Type! " + o.getClass().getName() + " cannot be converted to " + clazz.getName());
                }

                return ret;
            }

        }

        if (!clazz.isAssignableFrom(o.getClass())) {
            throw new IllegalStateException("Invalid Type! " + o.getClass().getName() + " cannot be converted to " + clazz.getName());
        }

        return (T) o;
    }


    private <T> boolean canConvert(Object o, Class<T> clazz) {

        if(reg != null) {
            if(reg.canSerialize(clazz) && o instanceof ConfigSection) {
                ConfigSerializer<T> ser = reg.getSerializer(clazz);
                T ret = ser.deserialize((ConfigSection) o);
                return ret != null;
            }
            if(reg.canSerializeInline(clazz)) {

                InlineSerializer<T> ser = reg.getInlineSerializer(clazz);
                T ret = ser.deserialize(o.toString());
                return ret != null;
            }

        }

        return clazz.isAssignableFrom(o.getClass());
    }

    public JsonObject toJson() {

        return (JsonObject) toJsonElement(this);
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public String toNBT() {

        StringBuilder builder = new StringBuilder("{");

        List<String> keys = new ArrayList<>();

        for(String s : getKeys()) {
            keys.add(s);
        }

        for(int i = 0 ; i < keys.size() ; i++) {

            if(i > 0) {
                builder.append(",");
            }

            String s = keys.get(i);
            Object o = get(s);
            builder.append("\"").append(s).append("\":").append(toNBTString(o));
        }

        builder.append("}");
        return builder.toString();
    }

    private static String toNBTString(Object o) {

        StringBuilder builder = new StringBuilder();

        if(o instanceof ConfigSection) {

            builder.append(((ConfigSection) o).toNBT());

        } else if(o instanceof List<?> l) {

            builder.append("[");

            int ints = 0;
            int longs = 0;
            int bytes = 0;

            for(Object obj : l) {
                if(obj instanceof Integer) ints++;
                if(obj instanceof Long) longs++;
                if(obj instanceof Byte) bytes++;
            }

            if(ints == l.size()) {
                builder.append("I;");
            } else if(longs == l.size()) {
                builder.append("L;");
            } else if(bytes == l.size()) {
                builder.append("B;");
            }

            for(int i = 0 ; i < l.size() ; i++) {
                if(i > 0) {
                    builder.append(",");
                }
                builder.append(toNBTString(l.get(i)));
            }

            builder.append("]");

        } else if(o instanceof String) {

            builder.append("\"").append(o.toString().replace("\"", "\\\"")).append("\"");

        } else {

            builder.append(o.toString());
        }

        return builder.toString();
    }

    private static JsonElement toJsonElement(Object obj) {

        if(obj instanceof ConfigSection sec) {

            JsonObject out = new JsonObject();
            for(String s : sec.getKeys()) {

                out.add(s, toJsonElement(sec.get(s)));
            }

            return out;

        } else if(obj instanceof List<?> lst) {

            JsonArray arr = new JsonArray();

            for (Object o : lst) {
                arr.add(toJsonElement(o));
            }
            return arr;

        } else if(obj instanceof Number) {

            return new JsonPrimitive((Number) obj);

        } else if(obj instanceof Boolean) {

            return new JsonPrimitive((Boolean) obj);

        } else {

            return new JsonPrimitive(obj.toString());
        }
    }

}

