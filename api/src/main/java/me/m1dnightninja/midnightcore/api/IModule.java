package me.m1dnightninja.midnightcore.api;

import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.util.List;

public interface IModule {

    boolean initialize(ConfigSection configuration);

    ModuleIdentifier getId();

    ConfigSection getDefaultConfig();

    default List<Class<? extends IModule>> getDependencies() { return null; }

}

