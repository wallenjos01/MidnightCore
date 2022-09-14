package org.wallentines.midnightcore.spigot.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.midnightlib.config.ConfigSection;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class YamlConfigProvider implements ConfigProvider {

    public static final YamlConfigProvider INSTANCE = new YamlConfigProvider();

    @Override
    public ConfigSection loadFromFile(File file) {
        try {
            return loadFromStream(new FileInputStream(file));
        } catch (FileNotFoundException ex) {
            return null;
        }
    }

    @Override
    public ConfigSection loadFromStream(InputStream stream) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
        return fromYaml(config);
    }

    @Override
    public ConfigSection loadFromString(String string) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(string));
        return fromYaml(config);
    }

    @Override
    public void saveToFile(ConfigSection config, File file) {

        YamlConfiguration out = toYaml(config);
        try {
            out.save(file);
        } catch (IOException ex) {
            // Ignore
        }
    }

    @Override
    public String saveToString(ConfigSection config) {

        YamlConfiguration out = toYaml(config);
        return out.saveToString();
    }

    @Override
    public String getFileExtension() {
        return ".yml";
    }

    private Object serializeForYaml(Object value) {

        if(value instanceof ConfigSection) {

            return toYaml((ConfigSection) value);

        } else if(value instanceof Collection<?>) {

            List<Object> list = new ArrayList<>();
            for(Object o : (Collection<?>) value) {
                list.add(serializeForYaml(o));
            }
            return list;

        } else {

            return value;
        }
    }

    public YamlConfiguration toYaml(ConfigSection sec) {

        YamlConfiguration out = new YamlConfiguration();
        for(String key : sec.getKeys()) {

            Object value = sec.get(key);
            out.set(key, serializeForYaml(value));
        }

        return out;
    }

    private Object deserializeFromYaml(Object value) {

        if(value instanceof ConfigurationSection) {

            return fromYaml((ConfigurationSection) value);

        } else if(value instanceof Collection<?>) {

            List<Object> list = new ArrayList<>();
            for(Object o : (Collection<?>) value) {
                list.add(deserializeFromYaml(o));
            }
            return list;

        } else {

            return value;
        }
    }

    public ConfigSection fromYaml(ConfigurationSection config) {

        ConfigSection out = new ConfigSection();
        if(config == null) return null;

        for(String key : config.getKeys(false)) {

            Object o = config.get(key);
            out.set(key, deserializeFromYaml(o));
        }

        return out;
    }
}
