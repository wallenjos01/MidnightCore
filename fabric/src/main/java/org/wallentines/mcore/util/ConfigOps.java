package org.wallentines.mcore.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.wallentines.mdcfg.ConfigList;
import org.wallentines.mdcfg.ConfigObject;
import org.wallentines.mdcfg.ConfigPrimitive;
import org.wallentines.mdcfg.ConfigSection;

import java.util.stream.Stream;

public class ConfigOps implements DynamicOps<ConfigObject> {

    public static final ConfigOps INSTANCE = new ConfigOps();

    @Override
    public ConfigObject empty() {
        return ConfigPrimitive.NULL;
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, ConfigObject input) {

        if(input == null) {
            return outOps.empty();
        }

        if (input.isSection()) {
            return convertMap(outOps, input);
        }
        if (input.isList()) {
            return convertList(outOps, input);
        }
        if (input.isString()) {
            return outOps.createString(input.asString());
        }
        if (input.isBoolean()) {
            return outOps.createBoolean(input.asBoolean());
        }
        if(input.isNumber()) {
            return outOps.createNumeric(input.asNumber());
        }

        return null;
    }

    @Override
    public DataResult<Number> getNumberValue(ConfigObject input) {
        if(input.isNumber()) {
            return DataResult.success(input.asNumber());
        }
        return DataResult.error(() -> "Not a number!");
    }

    @Override
    public ConfigObject createNumeric(Number i) {
        return new ConfigPrimitive(i);
    }

    @Override
    public DataResult<String> getStringValue(ConfigObject input) {
        if(input.isString()) {
            return DataResult.success(input.asString());
        }
        return DataResult.error(() -> "Not a string!");
    }

    @Override
    public ConfigObject createString(String value) {
        return new ConfigPrimitive(value);
    }

    @Override
    public DataResult<ConfigObject> mergeToList(ConfigObject list, ConfigObject value) {

        if(list != null && !list.isList() && !list.equals(empty())) return DataResult.error(() -> "Not a list!");

        ConfigList output;
        if(list != null && list.isList()) {
            output = list.asList().copy();
        } else {
            output = new ConfigList();
        }
        output.asList().add(value);

        return DataResult.success(output);
    }

    @Override
    public DataResult<ConfigObject> mergeToMap(ConfigObject map, ConfigObject key, ConfigObject value) {

        if(map != null && !map.isSection() && !map.equals(empty())) return DataResult.error(() -> "Not a map!");
        if(!key.isString()) return DataResult.error(() -> "Key was not a String!");

        ConfigSection output;
        if(map != null && map.isSection()) {
            output = map.asSection().copy();
        } else {
            output = new ConfigSection();
        }
        output.set(key.asString(), value);

        return DataResult.success(output);
    }

    @Override
    public DataResult<Stream<Pair<ConfigObject, ConfigObject>>> getMapValues(ConfigObject input) {

        if(input != null && input.isSection()) {
            return DataResult.success(input.asSection().getKeys().stream().map(key -> Pair.of(new ConfigPrimitive(key), input.asSection().get(key))));
        }

        return DataResult.error(() -> "Not a section!");
    }

    @Override
    public ConfigObject createMap(Stream<Pair<ConfigObject, ConfigObject>> map) {
        ConfigSection out = new ConfigSection();
        map.forEach(p -> {
            if(p.getFirst().isString()) {
                out.set(p.getFirst().asString(), p.getSecond());
            }
        });
        return out;
    }

    @Override
    public DataResult<Stream<ConfigObject>> getStream(ConfigObject input) {
        if(input.isList()) {
            return DataResult.success(input.asList().values().stream());
        }
        return DataResult.error(() -> "Not a list!");
    }

    @Override
    public ConfigObject createList(Stream<ConfigObject> input) {

        ConfigList out = new ConfigList();
        input.forEach(out::add);

        return out;
    }

    @Override
    public ConfigObject remove(ConfigObject input, String key) {

        if(input.isSection()) {
            return input.asSection().remove(key);
        }

        return input;
    }
}
