package me.m1dnightninja.midnightcore.api.module.playerdata;

import me.m1dnightninja.midnightcore.api.module.IModule;

import java.io.File;

public interface IPlayerDataModule extends IModule {

    IPlayerDataProvider getGlobalProvider();

    IPlayerDataProvider createProvider(File folder);

}
