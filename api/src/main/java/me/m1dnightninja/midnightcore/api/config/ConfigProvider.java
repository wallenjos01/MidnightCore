package me.m1dnightninja.midnightcore.api.config;

import java.io.File;

public interface ConfigProvider {

    ConfigSection loadFromFile(File f);

    void saveToFile(ConfigSection s, File f);

}
