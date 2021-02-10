package me.m1dnightninja.midnightcore.spigot.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class YamlConfigProvider implements ConfigProvider {

    @Override
    public ConfigSection loadFromFile(File file) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return fromYaml(config);
    }

    @Override
    public void saveToFile(ConfigSection config, File file) {

        ConfigurationSection sec = toYaml(config);
        if(sec == null) return;

        YamlConfiguration out = (YamlConfiguration) sec;
        try {
            out.save(file);
        } catch (IOException ex) {
            MidnightCoreAPI.getLogger().warn("An error occurred while trying to save a config file!");
            ex.printStackTrace();
        }
    }

    private ConfigurationSection toYaml(ConfigSection sec) {

        if(sec.getEntries().size() == 0) return null;

        ConfigurationSection out = new YamlConfiguration();

        for(Map.Entry<String, Object> ent : sec.getEntries().entrySet()) {

            if(ent.getValue() instanceof ConfigSection) {
                out.set(ent.getKey(), toYaml((ConfigSection) ent.getValue()));
            } else {
                out.set(ent.getKey(), ent.getValue());
            }
        }

        return out;
    }

    public ConfigSection fromYaml(ConfigurationSection sec) {

        ConfigSection out = new ConfigSection();
        for(Map.Entry<String, Object> ent : sec.getValues(false).entrySet()) {

            if(ent.getValue() instanceof ConfigurationSection) {
                out.set(ent.getKey(), fromYaml((ConfigurationSection) ent.getValue()));
            } else {
                out.set(ent.getKey(), ent.getValue());
            }
        }

        return out;
    }
}
