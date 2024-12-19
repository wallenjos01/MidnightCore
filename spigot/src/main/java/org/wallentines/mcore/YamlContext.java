package org.wallentines.mcore;

import org.bukkit.configuration.ConfigurationSection;
import org.wallentines.mdcfg.serializer.SerializeContext;
import org.wallentines.mdcfg.serializer.SerializeResult;

import java.nio.ByteBuffer;
import java.util.*;

public class YamlContext implements SerializeContext<Object> {

    public static final YamlContext INSTANCE = new YamlContext();

    @Override
    public SerializeResult<String> asString(Object object) {
        return isString(object)
                ? SerializeResult.success((String) object)
                : SerializeResult.failure("Not a string");
    }

    @Override
    public SerializeResult<Number> asNumber(Object object) {
        return isNumber(object)
                ? SerializeResult.success((Number) object)
                : SerializeResult.failure("Not a number");
    }

    @Override
    public SerializeResult<Boolean> asBoolean(Object object) {
        return isBoolean(object)
                ? SerializeResult.success((Boolean) object)
                : SerializeResult.failure("Not a boolean");
    }

    @Override
    public SerializeResult<ByteBuffer> asBlob(Object object) {
        return isBlob(object)
                ? SerializeResult.success((ByteBuffer) object)
                : SerializeResult.failure("Not a blob");
    }

    @SuppressWarnings("unchecked")
    @Override
    public SerializeResult<Collection<Object>> asList(Object object) {
        return isList(object)
                ? SerializeResult.success((Collection<Object>) object)
                : SerializeResult.failure("Not a list");
    }

    @SuppressWarnings("unchecked")
    @Override
    public SerializeResult<Map<String, Object>> asMap(Object object) {
        if(!isMap(object)) return SerializeResult.failure("Not a map");
        if(object instanceof Map) return SerializeResult.success((Map<String, Object>) object);

        Map<String, Object> out = new HashMap<>();
        addMapEntries((ConfigurationSection) object, out);
        return SerializeResult.success(out);
    }

    @SuppressWarnings("unchecked")
    @Override
    public SerializeResult<Map<String, Object>> asOrderedMap(Object object) {
        if(!isMap(object)) return SerializeResult.failure("Not a map");
        if(object instanceof Map) return SerializeResult.success((Map<String, Object>) object);

        Map<String, Object> out = new LinkedHashMap<>();
        addMapEntries((ConfigurationSection) object, out);
        return SerializeResult.success(out);
    }

    private void addMapEntries(ConfigurationSection object, Map<String, Object> out) {
        for(String key : object.getKeys(false)) {
            Object o = object.get(key);
            out.put(key, serializeObject(o));
        }
    }

    @Override
    public Type getType(Object object) {
        if(object == null) return Type.NULL;
        if(object instanceof String) return Type.STRING;
        if(object instanceof Number) return Type.NUMBER;
        if(object instanceof Boolean) return Type.BOOLEAN;
        if(object instanceof ByteBuffer) return Type.BLOB;
        if(object instanceof Collection) return Type.LIST;
        if(object instanceof ConfigurationSection || object instanceof Map) return Type.MAP;
        return Type.UNKNOWN;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<String> getOrderedKeys(Object object) {
        if(!isMap(object)) return null;
        return object instanceof Map ?
                ((Map<String, ?>) object).keySet() :
                ((ConfigurationSection) object).getKeys(false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get(String key, Object object) {
        if(!isMap(object)) return null;

        return object instanceof Map ?
                ((Map<String, ?>) object).get(key) :
                ((ConfigurationSection) object).get(key);
    }

    @Override
    public Object toString(String object) {
        return object;
    }

    @Override
    public Object toNumber(Number object) {
        return object;
    }

    @Override
    public Object toBoolean(Boolean object) {
        return object;
    }

    @Override
    public Object toBlob(ByteBuffer object) {
        return object;
        //return Base64.getEncoder().encode(object).asCharBuffer().toString();
    }

    @Override
    public Object toList(Collection<Object> list) {

        List<Object> out = new ArrayList<>();
        for(Object o : list) {
            out.add(serializeObject(o));
        }
        return out;
    }

    @Override
    public Object toMap(Map<String, Object> map) {

        LinkedHashMap<String, Object> sec = new LinkedHashMap<>();
        map.forEach((key, value) -> sec.put(key, serializeObject(value)));
        return sec;
    }

    @Override
    public Object nullValue() {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object set(String key, Object value, Object object) {
        if(!isMap(object)) return null;
        if(object instanceof Map) {
            ((Map<String, Object>) object).put(key, value);
        } else {
            ((ConfigurationSection) object).set(key, value);
        }
        return value;
    }

    @Override
    public boolean supportsMeta(Object o) {
        return false;
    }

    @Override
    public String getMetaProperty(Object o, String s) {
        return null;
    }

    @Override
    public void setMetaProperty(Object o, String s, String s1) {

    }

    private Object serializeObject(Object o) {
        if(isMap(o)) {
            return asMap(o);
        } else if(isList(o)) {
            return asList(o);
        } else {
            return o;
        }
    }
}
