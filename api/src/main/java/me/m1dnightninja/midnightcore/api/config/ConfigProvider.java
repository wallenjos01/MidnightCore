package me.m1dnightninja.midnightcore.api.config;

import java.io.File;

public interface ConfigProvider {

    ConfigSection loadFromFile(File file);

    void saveToFile(ConfigSection config, File file);

    String getFileExtension();
}

