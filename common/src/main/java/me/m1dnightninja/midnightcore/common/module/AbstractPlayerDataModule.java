package me.m1dnightninja.midnightcore.common.module;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;
import me.m1dnightninja.midnightcore.api.config.FileConfig;
import me.m1dnightninja.midnightcore.api.player.MPlayer;
import me.m1dnightninja.midnightcore.api.registry.MIdentifier;
import me.m1dnightninja.midnightcore.api.config.ConfigProvider;
import me.m1dnightninja.midnightcore.api.config.ConfigSection;
import me.m1dnightninja.midnightcore.api.module.IPlayerDataModule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public abstract class AbstractPlayerDataModule implements IPlayerDataModule {

    protected static final MIdentifier ID = MIdentifier.create("midnightcore","player_data");

    private File folder;
    private HashMap<UUID, ConfigSection> data;

    @Override
    public boolean initialize(ConfigSection configuration) {

        String fld_name = "data";
        if(configuration.has("folder_name")) {
            fld_name = configuration.getString("folder_name");
        }

        folder = new File(MidnightCoreAPI.getInstance().getDataFolder(), fld_name);
        if(!folder.exists() && !folder.mkdirs()) {

            MidnightCoreAPI.getLogger().warn("Unable to create player data folder!");
            return false;
        }

        data = new HashMap<>();
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
    public ConfigSection getPlayerData(UUID u) {

        if(!data.containsKey(u)) {
            loadData(u);
        }

        return data.get(u);
    }

    @Override
    public void setPlayerData(UUID u, ConfigSection sec) {

        data.put(u, sec);
    }

    @Override
    public void savePlayerData(UUID u) {

        if(!data.containsKey(u)) return;

        ConfigProvider prov = MidnightCoreAPI.getInstance().getDefaultConfigProvider();
        File file = new File(folder, u.toString() + prov.getFileExtension());

        prov.saveToFile(data.get(u), file);

        data.remove(u);
    }

    @Override
    public void reloadPlayerData(UUID u) {
        loadData(u);
    }

    @Override
    public void clearPlayerData(UUID u) {
        data.put(u, new ConfigSection());
        savePlayerData(u);
    }

    protected abstract void registerListeners();

    protected void onShutdown() {

        List<UUID> us = new ArrayList<>(data.keySet());
        for(UUID u : us) {
            savePlayerData(u);
        }
    }

    protected void onLeave(UUID u) {
        savePlayerData(u);
    }

    private void loadData(UUID u) {

        ConfigProvider prov = MidnightCoreAPI.getInstance().getDefaultConfigProvider();
        File f = new File(folder, u.toString() + prov.getFileExtension());

        if(!f.exists()) {
            data.put(u, new ConfigSection());
            return;
        }

        ConfigSection sec = prov.loadFromFile(f);
        data.put(u, sec);
    }
}
