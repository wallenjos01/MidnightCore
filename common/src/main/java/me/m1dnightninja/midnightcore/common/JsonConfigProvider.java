package me.m1dnightninja.midnightcore.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonConfigProvider implements ConfigProvider {

    @Override
    public ConfigSection loadFromFile(File file) {

        JsonWrapper f = JsonWrapper.loadFromFile(file);
        if(f == null) return new ConfigSection();

        return fromJson(f.getRoot());
    }

    @Override
    public void saveToFile(ConfigSection config, File file) {

        JsonWrapper f = new JsonWrapper(toJson(config));
        f.save(file);

    }

    private JsonObject toJson(ConfigSection sec) {

        JsonObject out = new JsonObject();
        for(Map.Entry<String,Object> ent : sec.getEntries().entrySet()) {

            out.add(ent.getKey(), toJsonElement(ent.getValue()));
        }

        return out;

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

    private JsonElement toJsonElement(Object obj) {

        if(obj instanceof ConfigSection) {

            return toJson((ConfigSection) obj);

        } else if(obj instanceof List) {

            JsonArray arr = new JsonArray();

            List<?> lst = (List<?>) obj;
            for(Object o : lst) {
                arr.add(toJsonElement(o));
            }

        } else if(obj instanceof Number) {

            return new JsonPrimitive((Number) obj);

        } else if(obj instanceof Boolean) {

            return new JsonPrimitive((Boolean) obj);

        } else {

            return new JsonPrimitive(obj.toString());
        }

        return null;

    }

}
