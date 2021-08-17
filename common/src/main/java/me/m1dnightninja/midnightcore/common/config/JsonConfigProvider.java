package me.m1dnightninja.midnightcore.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonConfigProvider implements ConfigProvider {

    public static final JsonConfigProvider INSTANCE = ConfigRegistry.INSTANCE.registerProvider(new JsonConfigProvider());

    @Override
    public ConfigSection loadFromFile(File file) {

        JsonWrapper w = JsonWrapper.loadFromFile(file);
        if(w == null || w.getRoot() == null) {
            return new ConfigSection();
        }

        return fromJson(w.getRoot());
    }

    @Override
    public ConfigSection loadFromStream(InputStream stream) {
        JsonWrapper w = new JsonWrapper();
        w.load(stream);

        return fromJson(w.getRoot());
    }

    public ConfigSection loadFromString(String s) {
        return loadFromStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void saveToFile(ConfigSection config, File file) {

        JsonWrapper w = new JsonWrapper(toJson(config));
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

    private JsonObject toJson(ConfigSection sec) {

        return sec.toJson();
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
}
