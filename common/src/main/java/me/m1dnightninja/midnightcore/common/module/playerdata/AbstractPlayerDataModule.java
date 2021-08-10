package me.m1dnightninja.midnightcore.common.module.playerdata;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataProvider;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.playerdata.IPlayerDataModule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractPlayerDataModule implements IPlayerDataModule {

    protected static final MIdentifier ID = MIdentifier.create("midnightcore","player_data");

    protected IPlayerDataProvider defaultProvider;
    protected List<IPlayerDataProvider> providers = new ArrayList<>();

    @Override
    public boolean initialize(ConfigSection configuration) {

        String fld_name = "data";
        if(configuration.has("folder_name")) {
            fld_name = configuration.getString("folder_name");
        }

        File folder = new File(MidnightCoreAPI.getInstance().getDataFolder(), fld_name);
        if(!folder.exists() && !folder.mkdirs()) {

            MidnightCoreAPI.getLogger().warn("Unable to create player data folder!");
            return false;
        }

        defaultProvider = createProvider(folder);
        registerListeners();

        return true;
    }

    @Override
    public MIdentifier getId() {
        return ID;
    }

    @Override
    public ConfigSection getDefaultConfig() {

        ConfigSection sec = new ConfigSection();
        sec.set("folder_name", "data");

        return sec;
    }

    @Override
    public IPlayerDataProvider createProvider(File folder) {

        IPlayerDataProvider out = new PlayerDataProvider(folder);
        providers.add(out);

        return out;
    }

    @Override
    public IPlayerDataProvider getGlobalProvider() {
        return defaultProvider;
    }

    protected abstract void registerListeners();

    protected void onShutdown() {

        for(IPlayerDataProvider prov : providers) {

            prov.saveAllPlayerData();
        }
    }

    protected void onLeave(UUID u) {

        for(IPlayerDataProvider prov : providers) {
            prov.savePlayerData(u);
        }
    }

}
