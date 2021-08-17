package me.m1dnightninja.midnightcore.spigot.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigRegistry;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlConfigProvider implements ConfigProvider {

    public static final YamlConfigProvider INSTANCE = ConfigRegistry.INSTANCE.registerProvider(new YamlConfigProvider());

    @Override
    public ConfigSection loadFromFile(File file) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        return sectionFromYaml(config);
    }

    @Override
    public ConfigSection loadFromStream(InputStream stream) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return sectionFromYaml(config);
    }

    @Override
    public void saveToFile(ConfigSection config, File file) {

        if(file.exists() && !file.delete()) {
            MidnightCoreAPI.getLogger().warn("Unable to clear " + file.getName() + "!");
        }

        YamlConfiguration sec = toYamlSection(config);
        if(sec == null) {
            return;
        }

        try {

            sec.save(file);
        } catch (IOException ex) {
            MidnightCoreAPI.getLogger().warn("An error occurred while trying to save a config file!");
            ex.printStackTrace();
        }
    }

    @Override
    public String saveToString(ConfigSection config) {

        YamlConfiguration sec = toYamlSection(config);
        if(sec == null) return "";

        return sec.saveToString();
    }

    @Override
    public String getFileExtension() {
        return ".yml";
    }

    private YamlConfiguration toYamlSection(ConfigSection sec) {

        if(sec.getEntries().size() == 0) return null;

        YamlConfiguration out = new YamlConfiguration();

        for(Map.Entry<String, Object> ent : sec.getEntries().entrySet()) {
            out.set(ent.getKey(), toYaml(ent.getValue()));
        }

        return out;
    }

    private Object toYaml(Object in) {

        if(in instanceof ConfigSection) {

            return toYamlSection((ConfigSection) in);

        } else if(in instanceof List) {

            List<Object> out = new ArrayList<>();
            for(Object o : (List<?>) in) {
                out.add(toYaml(o));
            }

            return out;

        } else {

            return in;
        }
    }

    public ConfigSection sectionFromYaml(ConfigurationSection sec) {

        ConfigSection out = new ConfigSection();
        for(Map.Entry<String, Object> ent : sec.getValues(false).entrySet()) {
            out.set(ent.getKey(), fromYaml(ent.getValue()));
        }

        return out;
    }

    public Object fromYaml(Object in) {

        if(in instanceof ConfigurationSection) {
            return sectionFromYaml((ConfigurationSection) in);

        } else if(in instanceof Map) {

            ConfigSection out = new ConfigSection();
            for(Map.Entry<?, ?> e : ((Map<?, ?>) in).entrySet()) {
                if(!(e.getKey() instanceof String)) continue;

                Object o = e.getValue();
                out.set((String) e.getKey(), fromYaml(o));
            }
            return out;

        } else if(in instanceof List) {

            List<Object> out = new ArrayList<>();

            for(Object o : (List<?>) in) {
                out.add(fromYaml(o));
            }

            return out;

        } else {

            return in;
        }
    }
}
