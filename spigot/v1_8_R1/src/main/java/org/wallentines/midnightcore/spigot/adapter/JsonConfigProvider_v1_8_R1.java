package org.wallentines.midnightcore.spigot.adapter;

import org.bukkit.craftbukkit.libs.com.google.gson.JsonArray;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonPrimitive;
import org.bukkit.craftbukkit.libs.com.google.gson.internal.LazilyParsedNumber;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.midnightlib.config.ConfigSection;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonConfigProvider_v1_8_R1 implements ConfigProvider {

    public static final JsonConfigProvider_v1_8_R1 INSTANCE = new JsonConfigProvider_v1_8_R1();

    @Override
    public ConfigSection loadFromFile(File file) {

        JsonWrapper_v1_8_R1 w = JsonWrapper_v1_8_R1.loadFromFile(file);
        if(w == null || w.getRoot() == null) {
            return new ConfigSection();
        }

        return fromJson(w.getRoot());
    }

    @Override
    public ConfigSection loadFromStream(InputStream stream) {
        JsonWrapper_v1_8_R1 w = new JsonWrapper_v1_8_R1();
        w.load(stream);

        return fromJson(w.getRoot());
    }

    public ConfigSection loadFromString(String s) {
        return loadFromStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void saveToFile(ConfigSection config, File file) {

        JsonWrapper_v1_8_R1 w = new JsonWrapper_v1_8_R1(toJson(config));
        w.save(file);

    }

    @Override
    public String saveToString(ConfigSection config) {
        JsonObject obj = toJson(config);

        return obj.toString();
    }

    @Override
    public String getFileExtension() {
        return ".json";
    }

    private ConfigSection fromJson(JsonObject obj) {

        ConfigSection out = new ConfigSection();

        for(Map.Entry<String, JsonElement> ele : obj.entrySet()) {

            out.set(ele.getKey(), fromJsonElement(ele.getValue()));
        }

        return out;

    }

    private Number fromJsonNumber(LazilyParsedNumber num) {

        String s = num.toString();
        if(s.contains(".")) {
            return num.doubleValue();
        } else {

            long lng = num.longValue();
            if(lng <= Integer.MAX_VALUE) {
                return num.intValue();
            }

            return lng;
        }
    }

    private Object fromJsonElement(JsonElement ele) {

        if(ele.isJsonObject()) {

            return fromJson(ele.getAsJsonObject());

        } else if(ele.isJsonArray()) {

            List<Object> lst = new ArrayList<>();

            for(JsonElement ele1 : ele.getAsJsonArray()) {
                lst.add(fromJsonElement(ele1));
            }

            return lst;

        } else if(ele.isJsonPrimitive()) {

            JsonPrimitive pr = ele.getAsJsonPrimitive();
            if(pr.isBoolean()) {
                return pr.getAsBoolean();
            } else if(pr.isNumber()) {

                Number num = pr.getAsNumber();

                if(num instanceof LazilyParsedNumber) {
                    return fromJsonNumber((LazilyParsedNumber) num);
                }

                return num;
            } else if(pr.isString()) {
                return pr.getAsString();
            }

        }

        return ele.toString();
    }

    public JsonObject toJson(ConfigSection sec) {

        return (JsonObject) toJsonElement(sec);
    }

    private static JsonElement toJsonElement(Object obj) {

        if(obj instanceof ConfigSection) {

            ConfigSection sec = (ConfigSection) obj;

            JsonObject out = new JsonObject();
            for(String s : sec.getKeys()) {

                out.add(s, toJsonElement(sec.get(s)));
            }

            return out;

        } else if(obj instanceof List<?>) {

            List<?> lst = (List<?>) obj;

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
