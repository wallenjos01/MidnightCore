package me.m1dnightninja.midnightcore.api.module;

import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;

import java.util.List;
import java.util.Set;

public interface IModule {

    boolean initialize(ConfigSection configuration);

    MIdentifier getId();

    ConfigSection getDefaultConfig();

    default Set<Class<? extends IModule>> getDependencies() { return null; }
    default void onDisable() { }

}

