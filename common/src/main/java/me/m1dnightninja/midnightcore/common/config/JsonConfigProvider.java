package me.m1dnightninja.midnightcore.common.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonConfigProvider implements ConfigProvider {

    public static final JsonConfigProvider INSTANCE = new JsonConfigProvider();

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
                return pr.getAsNumber();
            } else if(pr.isString()) {
                return pr.getAsString();
            }

        }

        return ele.toString();

    }
}
