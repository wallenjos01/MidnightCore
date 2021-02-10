package me.m1dnightninja.midnightcore.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;

public interface IModule {

    boolean initialize(ConfigSection configuration);

    String getId();

    ConfigSection getDefaultConfig();

}

