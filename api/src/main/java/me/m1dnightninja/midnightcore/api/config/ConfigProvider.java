package me.m1dnightninja.midnightcore.api.config;

import java.io.File;
import java.io.InputStream;

public interface ConfigProvider {

    ConfigSection loadFromFile(File file);

    ConfigSection loadFromStream(InputStream stream);

    void saveToFile(ConfigSection config, File file);

    String saveToString(ConfigSection config);

    String getFileExtension();
}

