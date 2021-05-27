package me.m1dnightninja.midnightcore.api.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.io.File;

public class FileConfig {

    private final File file;
    private final ConfigProvider provider;

    private ConfigSection root;

    public FileConfig(File f) {

        this(f, MidnightCoreAPI.getConfigRegistry().getProviderForFile(f));
    }

    public FileConfig(File f, ConfigProvider prov) {

        this.file = f;
        this.provider = prov;

        root = prov.loadFromFile(f);

    }

    public ConfigSection getRoot() {
        return root;
    }

    public void reload() {
        root = provider.loadFromFile(file);
    }

    public void save() {
        provider.saveToFile(root, file);
    }
}
