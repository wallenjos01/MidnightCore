package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.util.List;

public interface IModule {

    boolean initialize(ConfigSection configuration);

    MIdentifier getId();

    ConfigSection getDefaultConfig();

    default List<Class<? extends IModule>> getDependencies() { return null; }

}

