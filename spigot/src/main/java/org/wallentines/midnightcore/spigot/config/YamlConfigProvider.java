package org.wallentines.midnightcore.spigot.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.wallentines.midnightcore.api.MidnightCoreAPI;
import org.wallentines.midnightlib.config.ConfigProvider;
import org.wallentines.midnightlib.config.ConfigSection;

import java.io.*;

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

    public YamlConfiguration toYaml(ConfigSection sec) {

        YamlConfiguration out = new YamlConfiguration();
        for(String key : sec.getKeys()) {

            Object o = sec.get(key);
            if(o instanceof ConfigSection) {
                out.set(key, toYaml((ConfigSection) o));
            } else {
                out.set(key, o);
            }
        }

        return out;
    }

    public ConfigSection fromYaml(ConfigurationSection config) {

        ConfigSection out = new ConfigSection();
        if(config == null) return null;

        for(String key : config.getKeys(false)) {

            Object o = config.get(key);
            if(config.isConfigurationSection(key)) {
                out.set(key, fromYaml((ConfigurationSection) o));
            } else {
                out.set(key, o);
            }
        }

        return out;
    }
}
